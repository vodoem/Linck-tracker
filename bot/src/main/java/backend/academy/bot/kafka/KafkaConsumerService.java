package backend.academy.bot.kafka;

import backend.academy.bot.service.NotificationRouter;
import backend.academy.model.KafkaResponse;
import backend.academy.model.LinkUpdate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
// @ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaConsumerService {

    private final KafkaCommunicationService kafkaCommunicationService;
    private final NotificationRouter notificationRouter;

    public KafkaConsumerService(
            KafkaCommunicationService kafkaCommunicationService,
            NotificationRouter notificationRouter) {
        this.kafkaCommunicationService = kafkaCommunicationService;
        this.notificationRouter = notificationRouter;
    }

    @KafkaListener(topics = "${kafka.topic.response-topic}", groupId = "bot-group")
    public void handleResponse(ConsumerRecord<String, KafkaResponse> record) {
        KafkaResponse response = record.value(); // Извлекаем значение из ConsumerRecord
        kafkaCommunicationService.handleResponse(response);
    }

    @KafkaListener(topics = "${kafka.topic.link-updates}", groupId = "bot-group")
    public void handleLinkUpdate(LinkUpdate linkUpdate) {
        System.out.println("Получено обновление ссылки: " + linkUpdate);

        notificationRouter.dispatchUpdate(linkUpdate);
    }
}
