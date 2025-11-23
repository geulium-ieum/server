package seg.work.geuliumieum.server.config.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.util.ErrorHandler;
import seg.work.geuliumieum.server.common.audit.queue.AuditQueueMessage;
import seg.work.geuliumieum.server.common.entity.AuditLog;
import seg.work.geuliumieum.server.common.repository.AuditLogRepository;
import seg.work.geuliumieum.server.config.property.AuditQueueProperties;

@Slf4j
@Configuration
@EnableConfigurationProperties(AuditQueueProperties.class)
@RequiredArgsConstructor
public class AuditQueueConfig {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> auditStreamContainer(
        AsyncTaskExecutor auditAsyncExecutor,
        AuditQueueProperties props
    ) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
            (StreamMessageListenerContainerOptions) StreamMessageListenerContainerOptions.builder()
                .batchSize(props.getBatchSize())
                .executor(auditAsyncExecutor)
                .pollTimeout(Duration.ofMillis(props.getPollTimeoutMs()))
                .targetType(MapRecord.class)
                .errorHandler(shutdownTolerantErrorHandler())
                .build();

        return StreamMessageListenerContainer.create(stringRedisTemplate.getConnectionFactory(), options);
    }

    @Bean
    public Subscription auditStreamSubscription(
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> auditStreamContainer,
        AuditQueueProperties props
    ) {
        ensureGroup(props);
        return auditStreamContainer.receiveAutoAck(
            Consumer.from(props.getGroup(), consumerName(props)),
            StreamOffset.create(props.getStreamKey(), ReadOffset.lastConsumed()),
            this::handleMessage
        );
    }

    private void ensureGroup(AuditQueueProperties props) {
        try {
            // 그룹이 없으면 생성, 이미 있으면 예외 무시
            stringRedisTemplate.opsForStream().createGroup(props.getStreamKey(), ReadOffset.from("0-0"), props.getGroup());
        } catch (Exception e) {
            // BUSYGROUP 등은 무시
            log.debug("audit stream group create ignored: {}", e.getMessage());
        }
    }

    private String consumerName(AuditQueueProperties props) {
        if (props.getConsumerName() != null && !props.getConsumerName().isBlank()) {
            return props.getConsumerName();
        }
        String host = "unknown";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
        }
        return host + "-" + System.currentTimeMillis();
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        String json = message.getValue().get("json");
        if (json == null || json.isBlank()) {
            log.warn("Audit message without json field: id={}", message.getId());
            return;
        }
        try {
            AuditQueueMessage m = objectMapper.readValue(json, AuditQueueMessage.class);
            AuditLog entity = new AuditLog();
            entity.setAction(m.getAction().name());
            entity.setTargetType(m.getTargetType());
            entity.setTargetId(m.getTargetId());
            entity.setUserId(m.getUserId());
            entity.setUserAgent(m.getUserAgent());
            if (m.getIpAddress() != null && !m.getIpAddress().isBlank()) {
                try {
                    entity.setIpAddress(InetAddress.getByName(m.getIpAddress()));
                } catch (UnknownHostException ignored) {
                    // 저장 생략
                }
            }
            entity.setDetails(m.getDetails());
            auditLogRepository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to consume audit message: {}", e.getMessage());
        }
    }

    private ErrorHandler shutdownTolerantErrorHandler() {
        return ex -> {
            if (isBenignShutdownException(ex)) {
                // 셧다운 과정이나 이미 닫힌 커넥션으로 인한 예외는 디버그로만 남기고 무시
                log.debug("Audit stream container terminated while shutting down: {}", summarize(ex));
                return;
            }
            log.warn("Audit stream container error: {}", summarize(ex));
        };
    }

    private boolean isBenignShutdownException(Throwable ex) {
        // 애플리케이션 셧다운 신호가 왔다면 대부분의 Redis 폴링 오류는 무시 대상
        if (ShutdownManager.isShuttingDown()) {
            return true;
        }

        // 원인 체인을 따라가며 메시지/타입으로 판별
        Throwable t = ex;
        while (t != null) {
            String name = t.getClass().getName();
            String msg = String.valueOf(t.getMessage()).toLowerCase();

            // 공통 메시지 패턴
            if (msg.contains("connection is already closed") ||
                msg.contains("connection closed") ||
                msg.contains("connection reset") ||
                msg.contains("cancelled") ||
                msg.contains("canceled") ||
                msg.contains("handler removed") ||
                msg.contains("reactor.core.scheduler") ||
                msg.contains("rejectedexecutionexception") ||
                msg.contains("executor has been shutdown") ||
                msg.contains("pool is shut down")) {
                return true;
            }

            // 타입 기반 판별(의존성 추가 없이 FQCN 문자열 비교)
            if (name.startsWith("io.lettuce.core.")) {
                return true;
            }
            switch (name) {
                case "org.springframework.data.redis.RedisSystemException",
                     "org.springframework.data.redis.RedisConnectionFailureException",
                     "java.util.concurrent.RejectedExecutionException" -> {
                    return true;
                }
            }

            t = t.getCause();
        }
        return false;
    }

    private String summarize(Throwable ex) {
        // toString + root cause 메시지를 합쳐 간략히 표기
        StringBuilder sb = new StringBuilder(ex.toString());
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        if (root != ex) {
            sb.append("; root=").append(root.getClass().getName());
            if (root.getMessage() != null) {
                sb.append(": ").append(root.getMessage());
            }
        }
        return sb.toString();
    }
}
