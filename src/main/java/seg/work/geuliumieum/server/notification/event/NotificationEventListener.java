package seg.work.geuliumieum.server.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import seg.work.geuliumieum.server.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        notificationService.publish(
            event.getUserId(),
            event.getType(),
            event.getTitle(),
            event.getMessage(),
            event.getRelatedType(),
            event.getRelatedId()
        );
    }
}
