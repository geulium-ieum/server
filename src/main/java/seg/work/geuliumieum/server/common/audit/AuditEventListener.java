package seg.work.geuliumieum.server.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import seg.work.geuliumieum.server.common.audit.queue.AuditQueueProducer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditQueueProducer auditQueueProducer;

    @EventListener
    public void handle(AuditEvent event) {
        // 트랜잭션 안에서 발행되었으면 커밋 이후에 큐로 넣고,
        // 트랜잭션 밖(이미 커밋된 뒤)이면 즉시 큐로 넣는다.
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        auditQueueProducer.enqueue(event);
                    } catch (Exception e) {
                        log.warn("Failed to enqueue audit event(afterCommit): {}", e.getMessage());
                    }
                }
            });
        } else {
            try {
                auditQueueProducer.enqueue(event);
            } catch (Exception e) {
                log.warn("Failed to enqueue audit event(no-tx): {}", e.getMessage());
            }
        }
    }
}
