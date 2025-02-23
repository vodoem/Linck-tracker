package backend.academy.scrapper.repository;

import java.util.List;

public interface LinkRepository {
    void addLink(long chatId, String link);
    void removeLink(long chatId, String link);
    List<String> getLinks(long chatId);
    void registerChat(long chatId);
    void deleteChat(long chatId);
    List<Long> getAllChatIds();
}
