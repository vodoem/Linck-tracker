package backend.academy.scrapper.kafka;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.GetLinksByTagRequest;
import backend.academy.model.GetLinksRequest;
import backend.academy.model.GetTagsForLinkRequest;
import backend.academy.model.KafkaAddLinkRequest;
import backend.academy.model.KafkaAddTagsRequest;
import backend.academy.model.KafkaRemoveLinkRequest;
import backend.academy.model.KafkaRemoveTagRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.model.RemoveTagRequest;
import backend.academy.scrapper.service.LinkService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaConsumerService {

    private final LinkService linkService;
    private final KafkaCommunicationService kafkaCommunicationService;

    @Value("${app.db.batch-size}")
    private int batchSize;

    @Value("${kafka.topic.response-topic}")
    private String responseTopic;

    public KafkaConsumerService(LinkService linkService, KafkaCommunicationService kafkaCommunicationService) {
        this.linkService = linkService;
        this.kafkaCommunicationService = kafkaCommunicationService;
    }

    @KafkaListener(topics = "${kafka.topic.chat-register}", groupId = "scrapper-group")
    public void handleChatRegister(Long chatId) {
        linkService.registerChat(chatId);
    }

    @KafkaListener(topics = "${kafka.topic.chat-delete}", groupId = "scrapper-group")
    public void handleChatDelete(Long chatId) {
        linkService.deleteChat(chatId);
    }

    @KafkaListener(topics = "${kafka.topic.link-add}", groupId = "scrapper-group")
    public void handleAddLink(KafkaAddLinkRequest request) {
        System.out.println("Получен запрос на добавление ссылки: " + request);
        linkService.addLink(request.chatId(), new AddLinkRequest(request.link(), request.tags(), request.filters()));
    }

    @KafkaListener(topics = "${kafka.topic.link-remove}", groupId = "scrapper-group")
    public void handleRemoveLink(KafkaRemoveLinkRequest request) {
        System.out.println("Получен запрос на удаление ссылки: " + request);
        linkService.removeLink(request.chatId(), new RemoveLinkRequest(request.link()));
    }

    @KafkaListener(topics = "${kafka.topic.get-links-request}", groupId = "scrapper-group")
    public void handleGetLinksRequest(GetLinksRequest request) {
        System.out.println("Получен запрос на получение ссылок: " + request);

        int offset = 0;
        int limit = batchSize; // Размер пакета из конфигурации
        // Получаем ссылки из сервиса
        List<LinkResponse> links =
                linkService.getLinks(request.chatId(), offset, limit).links();

        kafkaCommunicationService.handleGetLinksRequest(request, links);
    }

    @KafkaListener(topics = "${kafka.topic.add-tags-request}", groupId = "scrapper-group")
    public void handleAddTags(KafkaAddTagsRequest request) {
        System.out.println("Получен запрос на добавление тегов: " + request);
        linkService.addTags(request.chatId(), request.url(), request.tags());
    }

    @KafkaListener(topics = "${kafka.topic.remove-tag-request}", groupId = "scrapper-group")
    public void handleRemoveTag(KafkaRemoveTagRequest request) {
        System.out.println("Получен запрос на удаление тега: " + request);
        linkService.removeTag(request.chatId(), new RemoveTagRequest(request.url(), request.tagName()));
    }

    @KafkaListener(topics = "${kafka.topic.get-tags-for-link-request}", groupId = "scrapper-group")
    public void handleGetTagsForLinkRequest(GetTagsForLinkRequest request) {
        System.out.println("Получен запрос на получение тегов для ссылки: " + request);
        List<String> tags = linkService.getTagsForLink(request.chatId(), request.url());
        kafkaCommunicationService.handleGetTagsForLinkRequest(request, tags);
    }

    @KafkaListener(topics = "${kafka.topic.get-links-by-tag-request}", groupId = "scrapper-group")
    public void handleGetLinksByTagRequest(GetLinksByTagRequest request) {
        System.out.println("Получен запрос на получение ссылок по тегу: " + request);
        List<LinkResponse> links =
                linkService.getLinksByTag(request.chatId(), request.tagName()).links();
        kafkaCommunicationService.handleGetLinksByTagRequest(request, links);
    }
}
