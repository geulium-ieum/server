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
    public Subscription auditStreamSubscription(AsyncTaskExecutor auditAsyncExecutor, AuditQueueProperties props) {
        ensureGroup(props);

        @SuppressWarnings({"rawtypes", "unchecked"})
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
            (StreamMessageListenerContainerOptions) StreamMessageListenerContainerOptions.builder()
                .batchSize(props.getBatchSize())
                .executor(auditAsyncExecutor)
                .pollTimeout(Duration.ofMillis(props.getPollTimeoutMs()))
                .targetType(MapRecord.class)
                .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
            StreamMessageListenerContainer.create(stringRedisTemplate.getConnectionFactory(), options);

        Subscription subscription = container.receiveAutoAck(
            Consumer.from(props.getGroup(), consumerName(props)),
            StreamOffset.create(props.getStreamKey(), ReadOffset.lastConsumed()),
            this::handleMessage
        );

        container.start();
        return subscription;
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
}
