package backend.academy.bot.service;

import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import java.util.List;

public interface CommunicationService {
    void registerChat(long chatId);

    void deleteChat(long chatId);

    void addLink(long chatId, String link, List<String> tags, List<String> filters);

    void removeLink(long chatId, String link);

    ListLinksResponse getLinks(long chatId);

    void addTags(long chatId, String url, List<String> tags);

    void removeTag(long chatId, String url, String tagName);

    List<String> getTagsForLink(long chatId, String url);

    List<LinkResponse> getLinksByTag(long chatId, String tagName);
}
