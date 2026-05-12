package backend.academy.bot.service;

import backend.academy.bot.kafka.KafkaCommunicationService;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DynamicCommunicationService implements CommunicationService {
    private final Map<String, CommunicationService> strategies;
    private String currentTransport;

    public DynamicCommunicationService(
            HttpCommunicationService httpCommunicationService,
            KafkaCommunicationService kafkaCommunicationService,
            @Value("${app.message-transport}") String initialTransport) {
        this.strategies = Map.of(
                "HTTP", httpCommunicationService,
                "Kafka", kafkaCommunicationService);
        this.currentTransport = initialTransport;
    }

    @Override
    public void registerChat(long chatId) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        strategy.registerChat(chatId);
    }

    @Override
    public void deleteChat(long chatId) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        strategy.deleteChat(chatId);
    }

    @Override
    public void addLink(long chatId, String link, List<String> tags, List<String> filters) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        strategy.addLink(chatId, link, tags, filters);
    }

    @Override
    public void removeLink(long chatId, String link) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        strategy.removeLink(chatId, link);
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        return strategy.getLinks(chatId);
    }

    @Override
    public void addTags(long chatId, String url, List<String> tags) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        strategy.addTags(chatId, url, tags);
    }

    @Override
    public void removeTag(long chatId, String url, String tagName) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        strategy.removeTag(chatId, url, tagName);
    }

    @Override
    public List<String> getTagsForLink(long chatId, String url) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        return strategy.getTagsForLink(chatId, url);
    }

    @Override
    public List<LinkResponse> getLinksByTag(long chatId, String tagName) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        return strategy.getLinksByTag(chatId, tagName);
    }

    public void setCurrentTransport(String transport) {
        if (!strategies.containsKey(transport)) {
            throw new IllegalArgumentException("Недопустимый транспорт: " + transport);
        }
        this.currentTransport = transport;
    }

    public String getCurrentTransport() {
        return currentTransport;
    }
}
