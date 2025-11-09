package seg.work.geuliumieum.server.util;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    @Setter
    private static RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장
    public static void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 만료시간이 있는 데이터 저장
    public static void setWithExpiryMs(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    public static void setWithExpirySec(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public static void setWithExpiryMin(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
    }

    public static void setWithExpiryHour(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.HOURS);
    }

    public static void setWithExpiryDay(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.DAYS);
    }

    /**
     * TimeUnit이 달은 지원하지 않으므로 timeout * 30으로 계산
     */
    public static void setWithExpiryMonth(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout * 30, TimeUnit.DAYS);
    }

    /**
     * TimeUnit이 년은 지원하지 않으므로 timeout * 365으로 계산
     */
    public static void setWithExpiryYear(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout * 365, TimeUnit.DAYS);
    }

    // 데이터 조회
    public static Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 데이터 조회 후 타입 변환
    public static String getStringValue(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static Long getLongValue(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    // 키 존재 확인
    public static boolean has(String key) {
        return redisTemplate.hasKey(key);
    }

    // 키 이름 변경
    public static void renameKey(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    // 변경예정 키 이름이 없을 경우에만 변경
    public static void renameKeyIfAbsent(String oldKey, String newKey) {
        redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    // 데이터 삭제
    public static void delete(String key) {
        redisTemplate.delete(key);
    }
}