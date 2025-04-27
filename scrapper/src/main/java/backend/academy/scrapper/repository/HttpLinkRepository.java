package backend.academy.scrapper.repository;

import backend.academy.model.LinkResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpLinkRepository implements LinkRepository {
    private final Map<Long, List<LinkResponse>> chatLinks =
            new ConcurrentHashMap<>(); // Храним ссылки для каждого chatId

    @Override
    public void addLink(long chatId, String link, List<String> tags, List<String> filters) {
        chatLinks
                .computeIfAbsent(chatId, k -> new ArrayList<>())
                .add(new LinkResponse(chatLinks.size() + 1, link, tags, filters));
    }

    @Override
    public void removeLink(long chatId, String link) {
        List<LinkResponse> links = chatLinks.getOrDefault(chatId, new ArrayList<>());

        // Удаляем объект LinkResponse, у которого поле url совпадает с переданной строкой
        links.removeIf(linkResponse -> linkResponse.url().equals(link));
    }

    @Override
    public List<LinkResponse> getLinks(long chatId, int offset, int limit) {
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

    @Override
    public void addTags(long chatId, String url, List<String> tags) {}

    @Override
    public void removeTag(long chatId, String url, String tagName) {}

    @Override
    public List<String> getTagsForLink(long chatId, String url) {
        return List.of();
    }

    @Override
    public List<LinkResponse> getLinksByTag(long chatId, String tagName) {
        return List.of();
    }

    @Override
    public boolean existsByChatIdAndUrl(long chatId, String url) {
        return false;
    }

    @Override
    public List<String> getFiltersForLink(long chatId, String url) {
        return List.of();
    }
}
