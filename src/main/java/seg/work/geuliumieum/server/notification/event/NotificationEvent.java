package seg.work.geuliumieum.server.notification.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationEvent {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String relatedType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long relatedId;
}
