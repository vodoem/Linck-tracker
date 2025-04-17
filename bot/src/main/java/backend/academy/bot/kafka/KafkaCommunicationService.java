package backend.academy.bot.kafka;

import backend.academy.bot.service.CommunicationService;
import backend.academy.bot.service.RedisCacheService;
import backend.academy.model.KafkaAddLinkRequest;
import backend.academy.model.KafkaAddTagsRequest;
import backend.academy.model.KafkaLinksResponse;
import backend.academy.model.KafkaRemoveLinkRequest;
import backend.academy.model.KafkaRemoveTagRequest;
import backend.academy.model.KafkaResponse;
import backend.academy.model.KafkaTagsResponse;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaCommunicationService implements CommunicationService {

    private final KafkaProducerService kafkaProducerService;
    private final Map<String, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();
    private final RedisCacheService redisCacheService;

    public KafkaCommunicationService(KafkaProducerService kafkaProducerService, RedisCacheService redisCacheService) {
        this.kafkaProducerService = kafkaProducerService;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void registerChat(long chatId) {
        kafkaProducerService.sendChatRegister(chatId);
    }

    @Override
    public void deleteChat(long chatId) {
        kafkaProducerService.sendChatDelete(chatId);
    }


    @Override
    public void addLink(long chatId, String link, List<String> tags, List<String> filters) {
        KafkaAddLinkRequest request = new KafkaAddLinkRequest(chatId, link, tags, filters);
        kafkaProducerService.sendLinkAdd(request);
        redisCacheService.invalidateCache(chatId);

    }

    @Override
    public void removeLink(long chatId, String link) {
        KafkaRemoveLinkRequest request = new KafkaRemoveLinkRequest(chatId, link);
        kafkaProducerService.sendLinkRemove(request);
        redisCacheService.invalidateCache(chatId);
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        ListLinksResponse cachedResponse = redisCacheService.getFromCache(chatId);
        if (cachedResponse != null) {
            System.out.println("Данные получены из кэша");
            return cachedResponse;
        }

        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ListLinksResponse> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        kafkaProducerService.sendGetLinksRequest(correlationId, chatId);

        try {
            ListLinksResponse response = future.get(1000, TimeUnit.SECONDS);
            redisCacheService.saveToCache(chatId, response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении ссылок через Kafka", e);
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    @Override
    public void addTags(long chatId, String url, List<String> tags) {
        String correlationId = UUID.randomUUID().toString();
        KafkaAddTagsRequest request = new KafkaAddTagsRequest(correlationId, chatId, url, tags);
        kafkaProducerService.sendAddTags(request);
    }

    @Override
    public void removeTag(long chatId, String url, String tagName) {
        String correlationId = UUID.randomUUID().toString();
        KafkaRemoveTagRequest request = new KafkaRemoveTagRequest(correlationId, chatId, url, tagName);
        kafkaProducerService.sendRemoveTag(request);
    }

    @Override
    public List<String> getTagsForLink(long chatId, String url) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        kafkaProducerService.sendGetTagsForLink(correlationId, chatId, url);

        try {
            return future.get(1000, TimeUnit.SECONDS); // Таймаут 5 секунд
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении тегов через Kafka", e);
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    @Override
    public List<LinkResponse> getLinksByTag(long chatId, String tagName) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<ListLinksResponse> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        kafkaProducerService.sendGetLinksByTag(correlationId, chatId, tagName);

        try {
            return future.get(1000, TimeUnit.SECONDS).links(); // Таймаут 5 секунд
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении ссылок по тегу через Kafka", e);
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public void handleResponse(KafkaResponse response) {
        switch (response) {
            case KafkaTagsResponse tagsResponse -> {
                CompletableFuture<List<String>> future =
                    (CompletableFuture<List<String>>) pendingRequests.get(tagsResponse.correlationId());
                if (future != null) {
                    future.complete(tagsResponse.tags());
                }
            }
            case KafkaLinksResponse linksResponse -> {
                CompletableFuture<ListLinksResponse> future =
                    (CompletableFuture<ListLinksResponse>) pendingRequests.get(linksResponse.correlationId());
                if (future != null) {
                    List<LinkResponse> links = linksResponse.links();
                    ListLinksResponse listLinksResponse = new ListLinksResponse(links, links.size());
                    future.complete(listLinksResponse);
                }
            }
        }
    }
}
