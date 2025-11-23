package seg.work.geuliumieum.server.common.audit.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import seg.work.geuliumieum.server.common.audit.AuditAction;
import seg.work.geuliumieum.server.common.audit.AuditEvent;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.config.property.AuditQueueProperties;
import seg.work.geuliumieum.server.config.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditQueueProducer {

    private static final String FIELD_JSON = "json";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AuditQueueProperties props;

    public void enqueue(AuditEvent event) {
        try {
            AuditQueueMessage msg = AuditQueueMessage.builder()
                .action(event.getAction())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .details(event.getDetails())
                .userId(currentUserId().orElse(null))
                .userAgent(extractUserAgent())
                .ipAddress(extractClientIp())
                .createdAt(Instant.now())
                .build();

            String json = objectMapper.writeValueAsString(msg);
            Map<String, String> body = new HashMap<>();
            body.put(FIELD_JSON, json);

            MapRecord<String, String, String> record = MapRecord.create(props.getStreamKey(), body);
            RecordId rid = stringRedisTemplate.opsForStream().add(record);
            if (rid == null) {
                log.warn("Failed to XADD audit message");
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit message: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to enqueue audit message: {}", e.getMessage());
        }
    }

    private Optional<Long> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserInfo ui) {
            return Optional.ofNullable(ui.getId());
        }
        if (principal instanceof CustomUserDetails cud) {
            return Optional.ofNullable(cud.getId());
        }
        return Optional.empty();
    }

    private String extractUserAgent() {
        HttpServletRequest req = currentRequest();
        return req == null ? null : req.getHeader("User-Agent");
    }

    private String extractClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) return null;
        String ip = firstNonEmpty(
            request.getHeader("X-Forwarded-For"),
            request.getHeader("X-Real-IP"),
            request.getRemoteAddr()
        );
        if (ip == null) return null;
        if (ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank() && !"unknown".equalsIgnoreCase(v)) {
                return v;
            }
        }
        return null;
    }
}
