package backend.academy.scrapper.service;

import backend.academy.scrapper.client.StackOverflowClient;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StackOverflowLinkChecker implements LinkChecker {
    private final StackOverflowClient stackOverflowClient;
    private final Map<String, Instant> lastUpdatedCache = new ConcurrentHashMap<>();

    public StackOverflowLinkChecker(StackOverflowClient stackOverflowClient) {
        this.stackOverflowClient = stackOverflowClient;
    }

    @Override
    public boolean checkForUpdates(String link) {
        String[] parts = link.split("/");
        int questionId = Integer.parseInt(parts[4]);
        String site = parts[2]; // Например, "ru.stackoverflow.com"

        long lastActivityDate = stackOverflowClient.getLastActivityDate(questionId, site);
        Instant lastActivityInstant = Instant.ofEpochSecond(lastActivityDate);

        String cachedKey = site + ":" + questionId;
        Instant cachedDate = lastUpdatedCache.getOrDefault(cachedKey, Instant.MIN);

        if (lastActivityInstant.isAfter(cachedDate)) {
            lastUpdatedCache.put(cachedKey, lastActivityInstant);
            return true;
        }
        return false;
    }

    @Override
    public String getUpdateDescription(String link) {
        String[] parts = link.split("/");
        int questionId = Integer.parseInt(parts[4]);
        return "Новый ответ на вопрос: " + questionId;
    }
}
