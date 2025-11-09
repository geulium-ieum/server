package seg.work.geuliumieum.server.config.property;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.data.redis.sentinel")
public class RedisSentinelProperties {

    private String master;
    private List<String> nodes;
    private String username;
    private String password;

    public RedisSentinelProperties(String master, List<String> nodes, String username, String password) {
        this.master = master;
        this.nodes = nodes;
        this.username = username;
        this.password = password;
    }
}