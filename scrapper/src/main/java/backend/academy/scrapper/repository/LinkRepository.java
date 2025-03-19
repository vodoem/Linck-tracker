package backend.academy.scrapper.repository;

import backend.academy.model.LinkResponse;
import java.util.List;

public interface LinkRepository {
    void addLink(long chatId, String link, List<String> tags, List<String> filters);

    void removeLink(long chatId, String link);

    List<LinkResponse> getLinks(long chatId, int offset, int limit);

    void registerChat(long chatId);

    void deleteChat(long chatId);

    List<Long> getAllChatIds();

    void addTags(long chatId, String url, List<String> tags);

    void removeTag(long chatId, String url, String tagName);

    List<String> getTagsForLink(long chatId, String url);

    List<LinkResponse> getLinksByTag(long chatId, String tagName);
}
