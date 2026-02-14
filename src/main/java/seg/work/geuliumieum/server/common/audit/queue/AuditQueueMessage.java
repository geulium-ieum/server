package seg.work.geuliumieum.server.common.audit.queue;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.audit.AuditAction;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditQueueMessage {

    private AuditAction action;
    private String targetType; // 엔티티 단순 클래스명
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String ipAddress;
    private String userAgent;

    private Map<String, Object> details;
    private Instant createdAt; // 큐 적재 시각
}
