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
}
