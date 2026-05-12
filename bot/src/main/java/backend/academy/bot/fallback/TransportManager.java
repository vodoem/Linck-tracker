package backend.academy.bot.fallback;

import backend.academy.bot.service.DynamicCommunicationService;
import org.springframework.stereotype.Component;

@Component
public class TransportManager {

    private final DynamicCommunicationService dynamicCommunicationService;
    private final TransportHealthChecker healthChecker;

    public TransportManager(
            DynamicCommunicationService dynamicCommunicationService, TransportHealthChecker healthChecker) {
        this.dynamicCommunicationService = dynamicCommunicationService;
        this.healthChecker = healthChecker;
    }

    public void checkAndSwitchTransport() {
        String currentTransport = dynamicCommunicationService.getCurrentTransport();

        if ("Kafka".equals(currentTransport) && !healthChecker.isKafkaAvailable()) {
            System.out.println("Kafka недоступен. Переключение на HTTP...");
            dynamicCommunicationService.setCurrentTransport("HTTP");
        } else if ("HTTP".equals(currentTransport) && !healthChecker.isHttpAvailable()) {
            System.out.println("HTTP недоступен. Переключение на Kafka...");
            dynamicCommunicationService.setCurrentTransport("Kafka");
        } else {
            System.out.println("Текущий транспорт доступен: " + currentTransport);
        }
    }
}
