package backend.academy.bot.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionService {
    private final Map<Long, List<ChatMessage>> historyByUser = new ConcurrentHashMap<>();

    public List<ChatMessage> getHistory(long userId) {
        return historyByUser.computeIfAbsent(userId, key -> new ArrayList<>());
    }

    public void appendUserMessage(long userId, String message) {
        historyByUser.computeIfAbsent(userId, key -> new ArrayList<>())
                .add(new ChatMessage("Вы", message));
    }

    public void appendBotMessage(long userId, String message) {
        historyByUser.computeIfAbsent(userId, key -> new ArrayList<>())
                .add(new ChatMessage("Бот", message));
    }

    public List<ChatMessage> immutableHistory(long userId) {
        return Collections.unmodifiableList(getHistory(userId));
    }
}
