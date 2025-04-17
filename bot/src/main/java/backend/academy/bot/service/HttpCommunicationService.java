package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP")
public class HttpCommunicationService implements CommunicationService {

    private final ScrapperClient scrapperClient;
    private final RedisCacheService redisCacheService;

    public HttpCommunicationService(ScrapperClient scrapperClient, RedisCacheService redisCacheService) {
        this.scrapperClient = scrapperClient;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void registerChat(long chatId) {
        scrapperClient.registerChat(chatId);
    }

    @Override
    public void deleteChat(long chatId) {
        scrapperClient.deleteChat(chatId);
    }


    @Override
    public void addLink(long chatId, String link, List<String> tags, List<String> filters) {
        scrapperClient.addLink(chatId, link, tags, filters);
        redisCacheService.invalidateCache(chatId);
    }

    @Override
    public void removeLink(long chatId, String link) {
        scrapperClient.removeLink(chatId, link);
        redisCacheService.invalidateCache(chatId);
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        ListLinksResponse cachedResponse = redisCacheService.getFromCache(chatId);
        if (cachedResponse != null) {
            System.out.println("Данные получены из кэша");
            return cachedResponse;
        }

        ListLinksResponse response = scrapperClient.getLinks(chatId);
        redisCacheService.saveToCache(chatId, response);
        return response;
    }

    @Override
    public void addTags(long chatId, String url, List<String> tags) {
        scrapperClient.addTags(chatId, url, tags);
    }

    @Override
    public void removeTag(long chatId, String url, String tagName) {
        scrapperClient.removeTag(chatId, url, tagName);
    }

    @Override
    public List<String> getTagsForLink(long chatId, String url) {
        return scrapperClient.getTagsForLink(chatId, url);
    }

    @Override
    public List<LinkResponse> getLinksByTag(long chatId, String tagName) {
        return scrapperClient.getLinksByTag(chatId, tagName);
    }
}
