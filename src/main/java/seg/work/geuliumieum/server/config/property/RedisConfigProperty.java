package seg.work.geuliumieum.server.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
@EnableConfigurationProperties(RedisSentinelProperties.class)
public class RedisConfigProperty {

    private String username;
    private String password;
    private int database = 0;
    private RedisSentinelProperties sentinel;
}