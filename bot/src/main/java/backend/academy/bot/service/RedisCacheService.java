package backend.academy.bot.service;

import backend.academy.model.ListLinksResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
            redisTemplate.opsForValue().set(getCacheKey(chatId), jsonData, 10, TimeUnit.MINUTES); // Кэш действителен 10 минут
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации данных в Redis", e);
        }
    }

    public void invalidateCache(long chatId) {
        redisTemplate.delete(getCacheKey(chatId));
    }
}
