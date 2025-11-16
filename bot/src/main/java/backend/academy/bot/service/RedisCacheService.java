package backend.academy.bot.service;

import backend.academy.model.ListLinksResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getCacheKey(long chatId) {
        return "links:" + chatId;
    }

    public ListLinksResponse getFromCache(long chatId) {
        String cachedData = redisTemplate.opsForValue().get(getCacheKey(chatId));
        if (cachedData != null) {
            try {
                return new ObjectMapper().readValue(cachedData, ListLinksResponse.class);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка десериализации данных из Redis", e);
            }
        }
        return null;
    }

    public void saveToCache(long chatId, ListLinksResponse response) {
        try {
            String jsonData = new ObjectMapper().writeValueAsString(response);
            redisTemplate
                    .opsForValue()
                    .set(getCacheKey(chatId), jsonData, 10, TimeUnit.MINUTES); // Кэш действителен 10 минут
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации данных в Redis", e);
        }
    }

    public void invalidateCache(long chatId) {
        redisTemplate.delete(getCacheKey(chatId));
    }

    // Ключ для режима уведомлений
    private String getNotificationModeKey(long chatId) {
        return "notification_mode:" + chatId;
    }

    // Получение режима уведомлений
    public String getNotificationMode(long chatId) {
        return redisTemplate.opsForValue().get(getNotificationModeKey(chatId));
    }

    // Установка режима уведомлений
    public void setNotificationMode(long chatId, String mode) {
        redisTemplate.opsForValue().set(getNotificationModeKey(chatId), mode);
    }

    // Ключ для накопления уведомлений
    private String getNotificationBatchKey(long chatId) {
        return "notification_batch:" + chatId;
    }

    // Добавление уведомления в батч
    public void addNotificationToBatch(long chatId, String notification) {
        redisTemplate.opsForList().rightPush(getNotificationBatchKey(chatId), notification);
    }

    // Получение всех уведомлений из батча
    public List<String> getNotificationsFromBatch(long chatId) {
        return redisTemplate.opsForList().range(getNotificationBatchKey(chatId), 0, -1);
    }

    // Очистка батча
    public void clearNotificationBatch(long chatId) {
        redisTemplate.delete(getNotificationBatchKey(chatId));
    }

    // Получение всех chatId с накопленными уведомлениями
    public List<Long> getAllChatIdsWithNotifications() {
        Set<String> keys = redisTemplate.keys("notification_batch:*");
        return Optional.ofNullable(keys)
                .orElse(Collections.emptySet()) // Возвращаем пустой Set, если keys == null
                .stream()
                .map(key -> Long.parseLong(key.replace("notification_batch:", "")))
                .toList();
    }

    public void clearAllNotificationData() {
        // Удаляем все ключи связанные с уведомлениями
        Set<String> keys = redisTemplate.keys("notification_*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void clearAllRedisKeys() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .flushAll();
    }
}
