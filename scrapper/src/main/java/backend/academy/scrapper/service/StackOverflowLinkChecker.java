package backend.academy.scrapper.service;

import backend.academy.scrapper.client.StackOverflowClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

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
        String site = parts[2];

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
        String site = parts[2];

        // Получаем данные о последнем ответе
        JsonNode answer = stackOverflowClient.getLastAnswer(questionId, site);
        if (answer == null) {
            return "Нет новых ответов.";
        }

        String userName = answer.get("owner").get("display_name").asText();
        long creationDate = answer.get("creation_date").asLong();
        String bodyPreview =
                answer.has("body") ? truncatePreview(answer.get("body").asText(), 200) : "Нет описания";

        String formattedDate = Instant.ofEpochSecond(creationDate)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return String.format(
                "Новый ответ на вопрос:%n" + "Автор: %s%n" + "Дата создания: %s%n" + "Превью ответа: %s",
                userName, formattedDate, bodyPreview);
    }

    private String truncatePreview(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }
}
