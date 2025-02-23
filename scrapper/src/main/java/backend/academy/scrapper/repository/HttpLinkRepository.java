package backend.academy.scrapper.repository;

import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class HttpLinkRepository implements LinkRepository {
    private final Map<Long, List<String>> chatLinks = new ConcurrentHashMap<>(); // Храним ссылки для каждого chatId

    @Override
    public void addLink(long chatId, String link) {
        chatLinks.computeIfAbsent(chatId, k -> new ArrayList<>()).add(link);
    }

    @Override
    public void removeLink(long chatId, String link) {
        chatLinks.getOrDefault(chatId, new ArrayList<>()).remove(link);
    }

    @Override
    public List<String> getLinks(long chatId) {
        return chatLinks.getOrDefault(chatId, Collections.emptyList());
    }

    @Override
    public void registerChat(long chatId) {
        chatLinks.putIfAbsent(chatId, new ArrayList<>());
    }

    @Override
    public void deleteChat(long chatId) {
        chatLinks.remove(chatId);
    }

    @Override
    public List<Long> getAllChatIds() {
        return new ArrayList<>(chatLinks.keySet());
    }
}
