package backend.academy.bot.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class BotStateMachine {
    private final Map<Long, String> states = new ConcurrentHashMap<>();
    private final Map<Long, String> pendingLinks = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> pendingTags = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> pendingFilters = new ConcurrentHashMap<>();

    public void setState(Long chatId, String state) {
        states.put(chatId, state);
    }

    public String getState(Long chatId) {
        return states.getOrDefault(chatId, "");
    }

    public void clearState(Long chatId) {
        states.remove(chatId);
        pendingLinks.remove(chatId);
        pendingTags.remove(chatId);
        pendingFilters.remove(chatId);
    }

    public void setPendingLink(Long chatId, String link) {
        pendingLinks.put(chatId, link);
    }

    public String getPendingLink(Long chatId) {
        return pendingLinks.get(chatId);
    }

    public void setPendingTags(Long chatId, List<String> tags) {
        pendingTags.put(chatId, tags);
    }

    public List<String> getPendingTags(Long chatId) {
        return pendingTags.getOrDefault(chatId, Collections.emptyList());
    }

    public void setPendingFilters(Long chatId, List<String> filters) {
        pendingFilters.put(chatId, filters);
    }

    public List<String> getPendingFilters(Long chatId) {
        return pendingFilters.getOrDefault(chatId, Collections.emptyList());
    }
}
