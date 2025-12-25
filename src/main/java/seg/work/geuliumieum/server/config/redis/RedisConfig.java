package seg.work.geuliumieum.server.config.redis;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import seg.work.geuliumieum.server.config.property.RedisConfigProperty;
import seg.work.geuliumieum.server.util.RedisUtil;

@Configuration
@EnableConfigurationProperties(RedisConfigProperty.class)
public class RedisConfig {

    private final RedisConfigProperty redisConfigProperty;

    public RedisConfig(RedisConfigProperty redisConfigProperty) {
        this.redisConfigProperty = redisConfigProperty;
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
            .master(redisConfigProperty.getSentinel().getMaster());

        List<String> nodes = redisConfigProperty.getSentinel().getNodes();
        for (String node : nodes) {
            sentinelConfig.sentinel(RedisNode.fromString(node));
        }

        sentinelConfig.setSentinelUsername(redisConfigProperty.getSentinel().getUsername());
        sentinelConfig.setSentinelPassword(RedisPassword.of(redisConfigProperty.getSentinel().getPassword()));
        sentinelConfig.setUsername(redisConfigProperty.getUsername());
        sentinelConfig.setPassword(RedisPassword.of(redisConfigProperty.getPassword()));
        sentinelConfig.setDatabase(redisConfigProperty.getDatabase());

        return new LettuceConnectionFactory(sentinelConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        redisTemplate.setEnableTransactionSupport(true);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        redisTemplate.afterPropertiesSet();

        RedisUtil.setRedisTemplate(redisTemplate);

        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory lettuceConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory lettuceConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory);
        return container;
    }
}