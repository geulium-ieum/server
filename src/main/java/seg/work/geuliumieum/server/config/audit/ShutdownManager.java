package seg.work.geuliumieum.server.config.audit;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 셧다운 진행 여부를 알리는 헬퍼.
 * Redis 컨슈머 오류 핸들러 등에서 셧다운 중 발생한 예외를 무시하기 위해 사용한다.
 */
@Component
public class ShutdownManager {

    private static final AtomicBoolean SHUTTING_DOWN = new AtomicBoolean(false);

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        SHUTTING_DOWN.set(true);
    }

    public static boolean isShuttingDown() {
        return SHUTTING_DOWN.get();
    }
}
