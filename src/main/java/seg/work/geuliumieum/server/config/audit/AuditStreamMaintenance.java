package seg.work.geuliumieum.server.config.audit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
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
    private final AtomicLong gaugePendingCount = new AtomicLong(0);
    private final AtomicLong gaugePendingMaxIdleMs = new AtomicLong(0);

    private Counter ctrAutoClaimed() {
        return meterRegistry.counter("audit.stream.autoclaimed");
    }

    private Counter ctrDlqMoved() {
        return meterRegistry.counter("audit.stream.dlq.moved");
    }

    // 게이지 등록 (lazy-init로 중복 등록 방지)
    private volatile boolean gaugesRegistered = false;

    private void ensureGauges() {
        if (!gaugesRegistered) {
            synchronized (this) {
                if (!gaugesRegistered) {
                    meterRegistry.gauge("audit.stream.length", gaugeStreamLen);
                    meterRegistry.gauge("audit.stream.dlq.length", gaugeDlqLen);
                    meterRegistry.gauge("audit.stream.pending.count", gaugePendingCount);
                    meterRegistry.gauge("audit.stream.pending.maxIdleMs", gaugePendingMaxIdleMs);
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
            if (len != null) {
                gaugeStreamLen.set(len);
            }
        } catch (Exception e) {
            log.debug("Failed to get stream length: {}", e.getMessage());
        }
        try {
            Long len = stringRedisTemplate.opsForStream().size(props.getDlqStreamKey());
            if (len != null) {
                gaugeDlqLen.set(len);
            }
        } catch (Exception e) {
            log.debug("Failed to get dlq length: {}", e.getMessage());
        }

        // XPENDING summary (group 기준)
        try {
            var summary = stringRedisTemplate.opsForStream().pending(props.getStreamKey(), props.getGroup());
            if (summary != null) {
                gaugePendingCount.set(summary.getTotalPendingMessages());
                // max idle 계산: summary는 최소/최대 ID와 consumer별 요약 제공. idle 시간은 제공되지 않으므로
                // 간단히 totalPending>0이면 대략적인 지표를 0으로 두고, 상세 idle은 고급 메트릭에서 보완할 수 있다.
                // 일부 드라이버에서는 pending(..., Range)로 세부 항목을 조회 가능하므로, 과도한 비용을 피하기 위해 생략.
                // 여기서는 pending count만 게이지로 노출.
            }
        } catch (Exception e) {
            log.debug("Failed to get XPENDING summary: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${audit.queue.trim-interval-ms:300000}")
    public void trimStreams() {
        if (!props.isMaintenanceEnabled()) {
            return;
        }
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

    /**
     * XAUTOCLAIM 대안: XPENDING(IDLE) + XCLAIM 저수준 명령으로 장기 pending 메시지 회수 처리. - idle > autoClaimIdleMs 인 항목을 조회하고, maintenance 컨슈머로 XCLAIM 하여 소유권을 이전. - dlqEnabled=true 이면 DLQ 스트림으로
     * 사본을 남긴 뒤, 원본을 XACK + XDEL 처리.
     */
    @Scheduled(fixedDelayString = "${audit.queue.auto-claim-interval-ms:60000}")
    public void reclaimPendingWithXclaim() {
        if (!props.isMaintenanceEnabled()) {
            return;
        }

        try {
            stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
                reclaimOnce(connection);
                return null;
            });
        } catch (Exception e) {
            log.debug("reclaim (XPENDING/XCLAIM) failed: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void reclaimOnce(RedisConnection connection) throws DataAccessException {
        final byte[] key = b(props.getStreamKey());
        final byte[] group = b(props.getGroup());
        final long idleMs = props.getAutoClaimIdleMs();
        final int max = Math.max(1, props.getAutoClaimMaxCount());

        // XPENDING key group IDLE <ms> - + <count>
        Object pendingResult = connection.execute(
            "XPENDING",
            key, group,
            b("IDLE"), b(Long.toString(idleMs)),
            b("-"), b("+"),
            b(Integer.toString(max))
        );

        if (!(pendingResult instanceof Iterable<?> iterable)) {
            return;
        }

        java.util.List<String> ids = new java.util.ArrayList<>();
        for (Object row : iterable) {
            // 각 row는 [id, consumer, idle, deliveries]
            if (row instanceof Iterable<?> cols) {
                java.util.Iterator<?> it = cols.iterator();
                if (it.hasNext()) {
                    Object idVal = it.next();
                    String id = s(idVal);
                    if (id != null) {
                        ids.add(id);
                    }
                }
            }
        }
        if (ids.isEmpty()) {
            return;
        }

        // XCLAIM key group consumer min-idle-time id [id ...]
        java.util.List<byte[]> args = new java.util.ArrayList<>();
        args.add(key);
        args.add(group);
        args.add(b("maintenance"));
        args.add(b(Long.toString(idleMs)));
        for (String id : ids) {
            args.add(b(id));
        }

        Object claimResult = connection.execute("XCLAIM", args.toArray(new byte[0][]));
        if (!(claimResult instanceof Iterable<?> claimed)) {
            return;
        }

        for (Object entry : claimed) {
            // entry 형식: [id, [ field, value, field, value, ... ]]
            String id = null;
            java.util.Map<String, String> fields = new java.util.HashMap<>();
            if (entry instanceof Iterable<?> cols) {
                java.util.Iterator<?> it = cols.iterator();
                if (it.hasNext()) {
                    id = s(it.next());
                }
                if (it.hasNext()) {
                    Object fv = it.next();
                    if (fv instanceof Iterable<?> fvPairs) {
                        java.util.Iterator<?> fvIt = fvPairs.iterator();
                        while (fvIt.hasNext()) {
                            Object f = fvIt.next();
                            if (!fvIt.hasNext()) {
                                break;
                            }
                            Object v = fvIt.next();
                            String fs = s(f);
                            String vs = s(v);
                            if (fs != null) {
                                fields.put(fs, vs);
                            }
                        }
                    }
                }
            }
            if (id == null) {
                continue;
            }

            try {
                if (props.isDlqEnabled()) {
                    // DLQ로 json 필드만 복사
                    String json = fields.get("json");
                    if (json != null) {
                        java.util.Map<String, String> body = java.util.Map.of("json", json);
                        connection.execute(
                            "XADD",
                            b(props.getDlqStreamKey()), b("*"),
                            b("json"), b(json)
                        );
                        ctrDlqMoved().increment();
                    }
                }
                // 원본 ack + xdel
                connection.execute("XACK", key, group, b(id));
                connection.execute("XDEL", key, b(id));
                ctrAutoClaimed().increment();
            } catch (Exception e) {
                log.debug("reclaim handle failed id={}: {}", id, e.getMessage());
            }
        }
    }

    private static byte[] b(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String s(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof byte[] ba) {
            return new String(ba, StandardCharsets.UTF_8);
        }
        return o.toString();
    }
}
