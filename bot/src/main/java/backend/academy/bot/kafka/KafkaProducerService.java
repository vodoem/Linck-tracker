package backend.academy.bot.kafka;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.AddTagsRequest;
import backend.academy.model.GetLinksByTagRequest;
import backend.academy.model.GetLinksRequest;
import backend.academy.model.GetTagsForLinkRequest;
import backend.academy.model.KafkaAddLinkRequest;
import backend.academy.model.KafkaAddTagsRequest;
import backend.academy.model.KafkaRemoveLinkRequest;
import backend.academy.model.KafkaRemoveTagRequest;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.model.RemoveTagRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.chat-register}")
    private String chatRegisterTopic;

    @Value("${kafka.topic.chat-delete}")
    private String chatDeleteTopic;

    @Value("${kafka.topic.link-add}")
    private String linkAddTopic;

    @Value("${kafka.topic.link-remove}")
    private String linkRemoveTopic;

    @Value("${kafka.topic.get-links-request}")
    private String getLinksRequestTopic;

    @Value("${kafka.topic.add-tags-request}")
    private String addTagsRequestTopic;

    @Value("${kafka.topic.remove-tag-request}")
    private String removeTagRequestTopic;

    @Value("${kafka.topic.get-tags-for-link-request}")
    private String getTagsForLinkRequestTopic;

    @Value("${kafka.topic.get-links-by-tag-request}")
    private String getLinksByTagRequestTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendChatRegister(long chatId) {
        kafkaTemplate.send(chatRegisterTopic, chatId);
    }

    public void sendChatDelete(long chatId) {
        kafkaTemplate.send(chatDeleteTopic, chatId);
    }

    public void sendLinkAdd(KafkaAddLinkRequest request) {
        kafkaTemplate.send(linkAddTopic, request);
    }

    public void sendLinkRemove(KafkaRemoveLinkRequest request) {
        kafkaTemplate.send(linkRemoveTopic, request);
    }

    public void sendGetLinksRequest(String correlationId, long chatId) {
        GetLinksRequest request = new GetLinksRequest(correlationId, chatId);
        kafkaTemplate.send(getLinksRequestTopic, request);
    }

    public void sendAddTags(KafkaAddTagsRequest request) {
        kafkaTemplate.send(addTagsRequestTopic, request);
    }

    public void sendRemoveTag(KafkaRemoveTagRequest request) {
        kafkaTemplate.send(removeTagRequestTopic, request);
    }

    public void sendGetTagsForLink(String correlationId, long chatId, String url) {
        GetTagsForLinkRequest request = new GetTagsForLinkRequest(correlationId, chatId, url);
        kafkaTemplate.send(getTagsForLinkRequestTopic, request);
    }

    public void sendGetLinksByTag(String correlationId, long chatId, String tagName) {
        GetLinksByTagRequest request = new GetLinksByTagRequest(correlationId, chatId, tagName);
        kafkaTemplate.send(getLinksByTagRequestTopic, request);
    }
}
