package backend.academy.bot.web;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ChatSessionService {
    private final Map<Long, List<ChatMessage>> historyByUser = new ConcurrentHashMap<>();
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();
    private static final long EMITTER_TIMEOUT = Duration.ofMinutes(10).toMillis();

    public List<ChatMessage> getHistory(long userId) {
        return historyByUser.computeIfAbsent(userId, key -> new ArrayList<>());
    }

    public void appendUserMessage(long userId, String message) {
        ChatMessage chatMessage = new ChatMessage("Вы", message);
        historyByUser.computeIfAbsent(userId, key -> new ArrayList<>())
                .add(chatMessage);
        notifyEmitters(userId, chatMessage);
    }

    public void appendBotMessage(long userId, String message) {
        ChatMessage chatMessage = new ChatMessage("Бот", message);
        historyByUser.computeIfAbsent(userId, key -> new ArrayList<>())
                .add(chatMessage);
        notifyEmitters(userId, chatMessage);
    }

    public List<ChatMessage> immutableHistory(long userId) {
        return Collections.unmodifiableList(getHistory(userId));
    }

    public SseEmitter registerEmitter(long userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        emittersByUser.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(throwable -> removeEmitter(userId, emitter));
        return emitter;
    }

    private void notifyEmitters(long userId, ChatMessage message) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("message").data(message));
            } catch (IOException e) {
                emitter.completeWithError(e);
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }
}
