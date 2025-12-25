package seg.work.geuliumieum.server.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationEvent {

    private Long userId;
    private String type;
    private String title;
    private String message;
    private String relatedType;
    private Long relatedId;
}
