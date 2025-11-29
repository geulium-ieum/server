package seg.work.geuliumieum.server.config.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
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
    private final MeterRegistry meterRegistry;

    private Counter ctrConsumed() { return meterRegistry.counter("audit.stream.consumed"); }
    private Counter ctrSaved() { return meterRegistry.counter("audit.stream.saved"); }
    private Counter ctrFailed() { return meterRegistry.counter("audit.stream.failed"); }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> auditStreamContainer(
        AsyncTaskExecutor auditAsyncExecutor,
        AuditQueueProperties props
    ) {
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
            StreamMessageListenerContainerOptions.builder()
                .batchSize(props.getBatchSize())
                .executor(auditAsyncExecutor)
                .pollTimeout(Duration.ofMillis(props.getPollTimeoutMs()))
                .errorHandler(shutdownTolerantErrorHandler())
                .build();

        assert stringRedisTemplate.getConnectionFactory() != null;
        return StreamMessageListenerContainer.create(stringRedisTemplate.getConnectionFactory(), options);
    }

    @Bean
    public Subscription auditStreamSubscription(
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> auditStreamContainer,
        AuditQueueProperties props
    ) {
        ensureGroup(props);
        // 수동 ACK 모드로 변경하여, DB 저장이 성공한 경우에만 ACK 하도록 함.
        return auditStreamContainer.receive(
            Consumer.from(props.getGroup(), consumerName(props)),
            StreamOffset.create(props.getStreamKey(), ReadOffset.lastConsumed()),
            message -> handleMessageWithAck(props, message)
        );
    }

    private void ensureGroup(AuditQueueProperties props) {
        // 스트림 미존재 환경에서 XGROUP CREATE가 실패할 수 있으므로 대비한다.
        // 1) 스트림이 없으면 더미 레코드로 생성한 뒤, 그룹 오프셋을 최신($)으로 생성하여 더미가 소비되지 않도록 한다.
        // 2) 스트림이 이미 있으면 0-0부터 소비할 수 있도록 그룹을 생성한다.
        try {
            String key = props.getStreamKey();
            Boolean exists = stringRedisTemplate.hasKey(key);
            if (!exists) {
                // 스트림 생성용 더미 레코드 추가
                stringRedisTemplate.opsForStream().add(
                    MapRecord.create(key, java.util.Map.of("init", "1"))
                );
                // 더미가 소비되지 않도록 최신 위치($)로 그룹 생성
                stringRedisTemplate.opsForStream().createGroup(key, ReadOffset.latest(), props.getGroup());
            } else {
                // 기존 스트림이면 처음부터(0-0) 소비하도록 그룹 생성
                stringRedisTemplate.opsForStream().createGroup(key, ReadOffset.from("0-0"), props.getGroup());
            }
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

    private void handleMessageWithAck(AuditQueueProperties props, MapRecord<String, String, String> message) {
        // 수신 카운트(원시 수신 기준)
        ctrConsumed().increment();
        String json = message.getValue().get("json");
        if (json == null || json.isBlank()) {
            // 더미 메시지 등 비정상 레코드. 운영 소음 방지를 위해 DEBUG로만 기록.
            log.debug("Audit message without json field: id={}", message.getId());
            // json이 없으면 재시도 가치가 낮으므로 ACK 처리하여 누적 방지
            try {
                stringRedisTemplate.opsForStream().acknowledge(props.getStreamKey(), props.getGroup(), message.getId());
                // 비정상 레코드는 즉시 삭제하여 누적 방지
                try {
                    stringRedisTemplate.opsForStream().delete(props.getStreamKey(), message.getId());
                } catch (Exception delEx) {
                    log.debug("XDEL failed for malformed message id={}: {}", message.getId(), delEx.getMessage());
                }
            } catch (Exception ex) {
                log.debug("ACK failed for malformed message id={}: {}", message.getId(), ex.getMessage());
            }
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
                    // IP 파싱 실패는 저장을 막지 않음
                }
            }
            entity.setDetails(m.getDetails());
            AuditLog saved;
            try {
                saved = auditLogRepository.save(entity);
            } catch (Exception saveEx) {
                // 데이터베이스에서 ipAddress 타입(예: PostgreSQL inet) 매핑 문제 가능성에 대비하여
                // ipAddress를 null로 재시도한다.
                if (entity.getIpAddress() != null) {
                    entity.setIpAddress(null);
                    saved = auditLogRepository.save(entity);
                } else {
                    throw saveEx;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("[AUDIT_CONSUME] saved id={} action={} type={} targetId={} userId={} recordId={}",
                    saved.getId(), saved.getAction(), saved.getTargetType(), saved.getTargetId(), saved.getUserId(), message.getId());
            }
            // 저장 성공 시에만 ACK
            try {
                stringRedisTemplate.opsForStream().acknowledge(props.getStreamKey(), props.getGroup(), message.getId());
                // 저장 성공한 레코드는 즉시 삭제하여 Stream 누적 방지
                try {
                    stringRedisTemplate.opsForStream().delete(props.getStreamKey(), message.getId());
                } catch (Exception delEx) {
                    log.debug("XDEL failed for message id={}: {}", message.getId(), delEx.getMessage());
                }
                ctrSaved().increment();
            } catch (Exception ex) {
                log.debug("ACK failed for message id={}: {}", message.getId(), ex.getMessage());
            }
        } catch (Exception e) {
            // 저장 실패 등 예외는 ACK하지 않아 재처리 대상(pending)으로 남긴다
            log.warn("Failed to consume audit message id={}: {}", message.getId(), e.getMessage());
            ctrFailed().increment();
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
