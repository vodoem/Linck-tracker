package backend.academy.bot.kafka;

import backend.academy.bot.client.TelegramClient;
import backend.academy.model.KafkaResponse;
import backend.academy.model.LinkUpdate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaConsumerService {

    private final TelegramClient telegramClient;
    private final KafkaCommunicationService kafkaCommunicationService;

    public KafkaConsumerService(TelegramClient telegramClient, KafkaCommunicationService kafkaCommunicationService) {
        this.telegramClient = telegramClient;
        this.kafkaCommunicationService = kafkaCommunicationService;
    }


    @KafkaListener(topics = "${kafka.topic.response-topic}", groupId = "bot-group")
    public void handleResponse(ConsumerRecord<String, KafkaResponse> record) {
        KafkaResponse response = record.value(); // Извлекаем значение из ConsumerRecord
        kafkaCommunicationService.handleResponse(response);
    }

    @KafkaListener(topics = "${kafka.topic.link-updates}", groupId = "bot-group")
    public void handleLinkUpdate(LinkUpdate linkUpdate) {
        System.out.println("Получено обновление ссылки: " + linkUpdate);
        // Логика обработки обновления (например, отправка уведомлений в Telegram)
        if (linkUpdate.tgChatIds() != null && !linkUpdate.tgChatIds().isEmpty()) {
            for (Long chatId : linkUpdate.tgChatIds()) {
                sendNotification(chatId, linkUpdate);
            }
        }
    }

    private void sendNotification(Long chatId, LinkUpdate update) {
        // Отправка уведомлений в чаты
        if (update.tgChatIds() != null && !update.tgChatIds().isEmpty()) {
            String message = "Обновление ссылки:\n"
                + "URL: " + update.url() + "\n"
                + "Описание: " + update.description();
            telegramClient.sendMessage(chatId, message);
        } else {
            System.out.println("Нет chatId для отправки уведомления.");
        }
    }
}
