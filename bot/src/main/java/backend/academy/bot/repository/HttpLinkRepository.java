package backend.academy.bot.repository;

import backend.academy.bot.client.ScrapperClient;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class HttpLinkRepository implements LinkRepository {
    private final ScrapperClient scrapperClient;
    private final Map<Long, List<String>> chatLinks = new ConcurrentHashMap<>(); // Храним ссылки для каждого chatId

    public HttpLinkRepository(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public void addLink(long chatId, String link) {
        chatLinks.computeIfAbsent(chatId, k -> new ArrayList<>()).add(link);
        scrapperClient.addLink(chatId, link, List.of(), List.of());
    }

    @Override
    public void removeLink(long chatId, String link) {
        chatLinks.getOrDefault(chatId, new ArrayList<>()).remove(link);
        scrapperClient.removeLink(chatId, link);
    }

    @Override
    public List<String> getLinks(long chatId) {
        return chatLinks.getOrDefault(chatId, Collections.emptyList());
    }

    @Override
    public void registerChat(long chatId) {
        chatLinks.putIfAbsent(chatId, new ArrayList<>());
        scrapperClient.registerChat(chatId);
    }

    @Override
    public List<Long> getAllChatIds() {
        return new ArrayList<>(chatLinks.keySet());
    }
}
