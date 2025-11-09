package seg.work.geuliumieum.server.config.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Spring Cache (Redis) 설정: 각 캐시 이름별 TTL/직렬화/프리픽스 제어.
 * <p>
 * - 기본 프리픽스: "cache:{cacheName}:" - 기본 TTL: 10분 - 캐시별 TTL 예시: - user:me -> 5분
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(LettuceConnectionFactory connectionFactory) {
        // Serializer 설정: 키 = String, 값 = JSON
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(SerializationPair.fromSerializer(keySerializer))
            .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer))
            .computePrefixWith(cacheName -> "cache:" + cacheName + ":")
            .entryTtl(Duration.ofMinutes(10));

        // 캐시 이름별 개별 TTL 정의
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("user:me", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
