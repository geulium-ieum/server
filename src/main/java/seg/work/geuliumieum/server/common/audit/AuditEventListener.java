package seg.work.geuliumieum.server.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import seg.work.geuliumieum.server.common.audit.queue.AuditQueueProducer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditQueueProducer auditQueueProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AuditEvent event) {
        try {
            auditQueueProducer.enqueue(event);
        } catch (Exception e) {
            log.warn("Failed to enqueue audit event: {}", e.getMessage());
        }
    }
}
