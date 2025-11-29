package seg.work.geuliumieum.server.config.audit;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import seg.work.geuliumieum.server.config.property.AuditQueueProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditStreamMaintenance {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuditQueueProperties props;
    private final MeterRegistry meterRegistry;

    private final AtomicLong gaugeStreamLen = new AtomicLong(0);
    private final AtomicLong gaugeDlqLen = new AtomicLong(0);

    // 게이지 등록 (lazy-init로 중복 등록 방지)
    private volatile boolean gaugesRegistered = false;

    private void ensureGauges() {
        if (!gaugesRegistered) {
            synchronized (this) {
                if (!gaugesRegistered) {
                    meterRegistry.gauge("audit.stream.length", gaugeStreamLen);
                    meterRegistry.gauge("audit.stream.dlq.length", gaugeDlqLen);
                    gaugesRegistered = true;
                }
            }
        }
    }

    @Scheduled(fixedDelayString = "${audit.queue.metrics-poll-ms:10000}")
    public void pollMetrics() {
        ensureGauges();
        try {
            Long len = stringRedisTemplate.opsForStream().size(props.getStreamKey());
            if (len != null) gaugeStreamLen.set(len);
        } catch (Exception e) {
            log.debug("Failed to get stream length: {}", e.getMessage());
        }
        try {
            Long len = stringRedisTemplate.opsForStream().size(props.getDlqStreamKey());
            if (len != null) gaugeDlqLen.set(len);
        } catch (Exception e) {
            log.debug("Failed to get dlq length: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${audit.queue.trim-interval-ms:300000}")
    public void trimStreams() {
        if (!props.isMaintenanceEnabled()) return;
        try {
            stringRedisTemplate.opsForStream().trim(props.getStreamKey(), props.getTrimMaxLen(), props.isTrimApproximate());
        } catch (Exception e) {
            log.debug("XTRIM failed for stream {}: {}", props.getStreamKey(), e.getMessage());
        }
        try {
            stringRedisTemplate.opsForStream().trim(props.getDlqStreamKey(), props.getTrimMaxLen(), props.isTrimApproximate());
        } catch (Exception e) {
            log.debug("XTRIM failed for dlq {}: {}", props.getDlqStreamKey(), e.getMessage());
        }
    }

    // 주: XAUTOCLAIM 기반 pending 회수는 현재 라이브러리 호환성 확인이 필요해 비활성화.
    //    Spring Data Redis 버전을 올리거나, 저수준 커맨드로 대체 구현 가능.
}
