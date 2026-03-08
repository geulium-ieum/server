package seg.work.geuliumieum.server.admin.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.AuditLog;

@Getter
@Builder
public class AdminAuditLogResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private LocalDateTime createdAt;
    private String action;
    private String targetType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> details;

    public static AdminAuditLogResponse from(AuditLog auditLog) {
        return AdminAuditLogResponse.builder()
            .id(auditLog.getId())
            .createdAt(auditLog.getCreatedAt())
            .action(auditLog.getAction())
            .targetType(auditLog.getTargetType())
            .targetId(auditLog.getTargetId())
            .userId(auditLog.getUserId())
            .ipAddress(auditLog.getIpAddress() == null ? null : auditLog.getIpAddress().getHostAddress())
            .userAgent(auditLog.getUserAgent())
            .details(auditLog.getDetails())
            .build();
    }
}
