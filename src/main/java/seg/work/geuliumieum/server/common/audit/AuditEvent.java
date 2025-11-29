package seg.work.geuliumieum.server.common.audit;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditEvent {

    private final AuditAction action;
    private final String targetType;
    private final Long targetId;
    private final Map<String, Object> details;
}
