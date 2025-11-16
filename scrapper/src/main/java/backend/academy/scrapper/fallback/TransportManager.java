package backend.academy.scrapper.fallback;

import backend.academy.scrapper.service.DynamicCommunicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransportManager {

    private final DynamicCommunicationService dynamicCommunicationService;
    private final TransportHealthChecker healthChecker;
    private final String preferredTransport;

    public TransportManager(
            DynamicCommunicationService dynamicCommunicationService,
            TransportHealthChecker healthChecker,
            @Value("${app.message-transport}") String preferredTransport) {
        this.dynamicCommunicationService = dynamicCommunicationService;
        this.healthChecker = healthChecker;
        this.preferredTransport = preferredTransport;
    }

    public void checkAndSwitchTransport() {
        String currentTransport = dynamicCommunicationService.getCurrentTransport();
        boolean httpAvailable = healthChecker.isHttpAvailable();
        boolean kafkaAvailable = healthChecker.isKafkaAvailable();

        if (!currentTransport.equals(preferredTransport)) {
            if ("HTTP".equals(preferredTransport) && httpAvailable) {
                System.out.println("HTTP восстановлен. Возвращаюсь к предпочтительному транспорту HTTP...");
                dynamicCommunicationService.setCurrentTransport("HTTP");
                return;
            } else if ("Kafka".equals(preferredTransport) && kafkaAvailable) {
                System.out.println("Kafka восстановлен. Возвращаюсь к предпочтительному транспорту Kafka...");
                dynamicCommunicationService.setCurrentTransport("Kafka");
                return;
            }
        }

        if ("Kafka".equals(currentTransport) && !kafkaAvailable && httpAvailable) {
            System.out.println("Kafka недоступен. Переключение на HTTP...");
            dynamicCommunicationService.setCurrentTransport("HTTP");
        } else if ("HTTP".equals(currentTransport) && !httpAvailable && kafkaAvailable) {
            System.out.println("HTTP недоступен. Переключение на Kafka...");
            dynamicCommunicationService.setCurrentTransport("Kafka");
        } else {
            System.out.println("Текущий транспорт доступен: " + currentTransport);
        }
    }
}
