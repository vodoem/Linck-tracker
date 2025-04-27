package backend.academy.scrapper.kafka;

import backend.academy.model.GetLinksByTagRequest;
import backend.academy.model.GetLinksRequest;
import backend.academy.model.GetTagsForLinkRequest;
import backend.academy.model.KafkaLinksResponse;
import backend.academy.model.KafkaTagsResponse;
import backend.academy.model.LinkResponse;
import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.service.CommunicationService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaCommunicationService implements CommunicationService {

    private final KafkaProducerService kafkaProducerService;

    public KafkaCommunicationService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public void sendUpdate(LinkUpdate update) {
        kafkaProducerService.sendUpdate(update);
    }

    public void handleGetLinksRequest(GetLinksRequest request, List<LinkResponse> links) {
        String correlationId = request.correlationId();
        kafkaProducerService.sendResponse(new KafkaLinksResponse(correlationId, links));
    }

    public void handleGetTagsForLinkRequest(GetTagsForLinkRequest request, List<String> tags) {
        String correlationId = request.correlationId();
        kafkaProducerService.sendResponse(new KafkaTagsResponse(correlationId, tags));
    }

    public void handleGetLinksByTagRequest(GetLinksByTagRequest request, List<LinkResponse> links) {
        String correlationId = request.correlationId();
        kafkaProducerService.sendResponse(new KafkaLinksResponse(correlationId, links));
    }
}
