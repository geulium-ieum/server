package seg.work.geuliumieum.server.notification.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import seg.work.geuliumieum.server.notification.dto.SsePayload;
import seg.work.geuliumieum.server.util.JsonUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService implements MessageListener {

    private static final long DEFAULT_TIMEOUT = 30L * 60L * 1000L; // 30 minutes
    private static final String REDIS_TOPIC_PREFIX = "sse:notification:";

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        List<SseEmitter> userEmitters = emitters.computeIfAbsent(userId, k -> {
            log.info("Adding Redis listener for user: {}", userId);
            redisMessageListenerContainer.addMessageListener(this, new ChannelTopic(REDIS_TOPIC_PREFIX + userId));
            return new ArrayList<>();
        });
        synchronized (userEmitters) {
            userEmitters.add(emitter);
        }

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // Send a comment/heartbeat to establish the stream
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Connected at " + java.time.LocalDateTime.now())
                .reconnectTime(3000));
        } catch (IOException ignored) {
        }
        return emitter;
    }

    public void sendNew(Long userId, Object payload) {
        publishToRedis(userId, "notification:new", payload);
    }

    public void sendRead(Long userId, Long notificationId) {
        publishToRedis(userId, "notification:read", notificationId);
    }

    private void publishToRedis(Long userId, String event, Object data) {
        SsePayload payload = new SsePayload(event, data);
        String json = JsonUtil.toJson(payload);
        if (json != null) {
            stringRedisTemplate.convertAndSend(REDIS_TOPIC_PREFIX + userId, json);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String channelIdStr = channel.substring(REDIS_TOPIC_PREFIX.length());
        try {
            Long userId = Long.parseLong(channelIdStr);
            String body = new String(message.getBody());

            SsePayload payload = JsonUtil.fromJson(body, SsePayload.class);
            if (payload != null) {
                sendToLocalEmitters(userId, payload.getEvent(), payload.getData());
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse userId from channel: {}", channel);
        }
    }

    private void sendToLocalEmitters(Long userId, String event, Object data) {
        List<SseEmitter> list = emitters.get(userId);
        if (list == null) {
            return;
        }
        List<SseEmitter> dead = new ArrayList<>();
        synchronized (list) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event()
                        .name(event)
                        .data(data, MediaType.APPLICATION_JSON));
                } catch (Exception e) {
                    dead.add(emitter);
                }
            }
            list.removeAll(dead);
            if (list.isEmpty()) {
                removeTopicListener(userId);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null) {
            synchronized (list) {
                list.remove(emitter);
                if (list.isEmpty()) {
                    removeTopicListener(userId);
                }
            }
        }
    }

    private void removeTopicListener(Long userId) {
        log.info("Removing Redis listener for user: {}", userId);
        emitters.remove(userId);
        redisMessageListenerContainer.removeMessageListener(this, new ChannelTopic(REDIS_TOPIC_PREFIX + userId));
    }
}
