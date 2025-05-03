package backend.academy.scrapper.service;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.kafka.KafkaCommunicationService;
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
    public void sendUpdate(LinkUpdate update) {
        CommunicationService strategy = strategies.get(currentTransport);
        if (strategy == null) {
            throw new IllegalStateException("Неизвестный транспорт: " + currentTransport);
        }
        strategy.sendUpdate(update);
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
