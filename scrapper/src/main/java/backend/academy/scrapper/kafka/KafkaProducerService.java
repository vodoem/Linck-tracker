package backend.academy.scrapper.kafka;

import backend.academy.model.KafkaLinksResponse;
import backend.academy.model.KafkaResponse;
import backend.academy.model.KafkaTagsResponse;
import backend.academy.model.LinkUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.link-updates}")
    private String linkUpdatesTopic;

    @Value("${kafka.topic.response-topic}")
    private String responseTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUpdate(LinkUpdate update) {
        kafkaTemplate.send(linkUpdatesTopic, update);
    }

    public void sendResponse(KafkaLinksResponse response) {
        kafkaTemplate.send(responseTopic, response);
    }

    public void sendResponse(KafkaTagsResponse response) {
        kafkaTemplate.send(responseTopic, response);
    }
}
