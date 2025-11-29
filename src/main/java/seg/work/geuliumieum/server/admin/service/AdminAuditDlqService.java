package seg.work.geuliumieum.server.admin.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.admin.dto.response.DlqReprocessResponse;
import seg.work.geuliumieum.server.config.property.AuditQueueProperties;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditDlqService {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuditQueueProperties props;
    private final MeterRegistry meterRegistry;

    public long size() {
        try {
            Long len = stringRedisTemplate.opsForStream().size(props.getDlqStreamKey());
            return len == null ? 0L : len;
        } catch (Exception e) {
            log.warn("Failed to get DLQ size: {}", e.getMessage());
            return 0L;
        }
    }

    public DlqReprocessResponse reprocess(int max) {
        int requested = max;
        int fetched = 0;
        int requeued = 0;
        int deleted = 0;
        int failed = 0;

        try {
            // DLQ에서 앞쪽부터 최대 max개 범위 조회
            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                .range(props.getDlqStreamKey(), Range.unbounded());

            if (records == null || records.isEmpty()) {
                return DlqReprocessResponse.builder()
                    .requested(requested)
                    .fetched(0)
                    .requeued(0)
                    .deleted(0)
                    .failed(0)
                    .build();
            }

            // 과거 버전 호환: range에 limit가 없으므로, 애플리케이션에서 상위 max건만 사용
            if (records.size() > max) {
                records = records.subList(0, max);
            }
            fetched = records.size();
            List<RecordId> idsToDelete = new ArrayList<>(fetched);

            for (MapRecord<String, Object, Object> rec : records) {
                try {
                    // 메인 스트림으로 재투입: DLQ의 value 중 json 필드만 보존하여 재발행
                    Object json = rec.getValue().get("json");
                    if (json instanceof String jsonStr) {
                        java.util.Map<String, String> body = java.util.Map.of("json", jsonStr);
                        MapRecord<String, String, String> mainRec = MapRecord.create(props.getStreamKey(), body);
                        stringRedisTemplate.opsForStream().add(mainRec);
                        requeued++;
                        idsToDelete.add(rec.getId());
                    } else {
                        // json 필드가 없거나 문자열이 아니면 스킵
                        failed++;
                        log.warn("DLQ record without valid json field id={} -> skip", rec.getId());
                    }
                } catch (Exception ex) {
                    failed++;
                    log.warn("DLQ reprocess add failed id={}: {}", rec.getId(), ex.getMessage());
                }
            }

            if (!idsToDelete.isEmpty()) {
                try {
                    // DLQ에서 삭제(XDEL) — 배치 삭제 (RecordId varargs 필요)
                    RecordId[] idArray = idsToDelete.toArray(RecordId[]::new);
                    Long delCount = stringRedisTemplate.opsForStream()
                        .delete(props.getDlqStreamKey(), idArray);
                    if (delCount != null) deleted = delCount.intValue();
                } catch (Exception ex) {
                    log.warn("DLQ delete failed: {}", ex.getMessage());
                }
            }

            // 메트릭 기록
            meterRegistry.counter("audit.stream.dlq.reprocess.requeued").increment(requeued);
            meterRegistry.counter("audit.stream.dlq.reprocess.failed").increment(failed);
            meterRegistry.counter("audit.stream.dlq.reprocess.deleted").increment(deleted);

            return DlqReprocessResponse.builder()
                .requested(requested)
                .fetched(fetched)
                .requeued(requeued)
                .deleted(deleted)
                .failed(failed)
                .build();
        } catch (Exception e) {
            log.warn("DLQ reprocess failed: {}", e.getMessage());
            return DlqReprocessResponse.builder()
                .requested(requested)
                .fetched(fetched)
                .requeued(requeued)
                .deleted(deleted)
                .failed(failed == 0 ? 1 : failed)
                .build();
        }
    }

    public long purge(Integer max) {
        try {
            if (max == null) {
                Long before = stringRedisTemplate.opsForStream().size(props.getDlqStreamKey());
                stringRedisTemplate.opsForStream().trim(props.getDlqStreamKey(), 0, true);
                return before == null ? 0L : before;
            }

            // 부분 퍼지: 앞쪽부터 최대 max개 아이디 조회 후 XDEL
            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                .range(props.getDlqStreamKey(), Range.unbounded());
            if (records == null || records.isEmpty()) return 0L;

            if (records.size() > max) {
                records = records.subList(0, max);
            }
            RecordId[] idArray = records.stream().map(MapRecord::getId).toArray(RecordId[]::new);
            Long del = stringRedisTemplate.opsForStream().delete(props.getDlqStreamKey(), idArray);
            return del == null ? 0L : del;
        } catch (Exception e) {
            log.warn("DLQ purge failed: {}", e.getMessage());
            return 0L;
        }
    }
}
