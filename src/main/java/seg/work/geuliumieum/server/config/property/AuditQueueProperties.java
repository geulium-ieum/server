package seg.work.geuliumieum.server.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "audit.queue")
public class AuditQueueProperties {
    /** Redis Stream key */
    private String streamKey = "audit:stream";
    /** Consumer Group name */
    private String group = "audit";
    /** Consumer name. 기본값은 호스트명-시간 */
    private String consumerName;

    /** 컨슈머 폴링 주기(ms) */
    private long pollTimeoutMs = 2000;
    /** 컨슈머 batch size */
    private int batchSize = 100;

    /** DLQ(Dead Letter Queue) Stream key */
    private String dlqStreamKey = "audit:dlq";

    /** XTRIM 유지 최대 길이 (approximate 적용 가능) */
    private long trimMaxLen = 1_000_000L;

    /** XTRIM 시 근사치(Approximate) 사용 여부 */
    private boolean trimApproximate = true;

    /** (선택) 유지보수 스케줄러 활성화 여부 */
    private boolean maintenanceEnabled = true;

    /** 메트릭 폴링 주기(ms) */
    private long metricsPollMs = 10_000L;

    /** XAUTOCLAIM idle 임계(ms). 이 시간 이상 pending이면 회수 대상 */
    private long autoClaimIdleMs = 10 * 60 * 1000L; // 10분

    /** XAUTOCLAIM 1회 처리 최대 레코드 수 */
    private int autoClaimMaxCount = 200;

    /** auto-claim된 레코드를 DLQ로 이동할지 여부 (true면 DLQ 이동 후 원본 ACK+XDEL) */
    private boolean dlqEnabled = true;

    /** 트림 작업 실행 간격(ms) */
    private long trimIntervalMs = 5 * 60 * 1000L; // 5분

    /** auto-claim 작업 실행 간격(ms) */
    private long autoClaimIntervalMs = 60 * 1000L; // 1분
}
