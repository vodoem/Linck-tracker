package backend.academy.bot.kafka;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.service.RedisCacheService;
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
    private final RedisCacheService redisCacheService;

    public KafkaConsumerService(TelegramClient telegramClient, KafkaCommunicationService kafkaCommunicationService, RedisCacheService redisCacheService) {
        this.telegramClient = telegramClient;
        this.kafkaCommunicationService = kafkaCommunicationService;
        this.redisCacheService = redisCacheService;
    }


    @KafkaListener(topics = "${kafka.topic.response-topic}", groupId = "bot-group")
    public void handleResponse(ConsumerRecord<String, KafkaResponse> record) {
        KafkaResponse response = record.value(); // Извлекаем значение из ConsumerRecord
        kafkaCommunicationService.handleResponse(response);
    }

    @KafkaListener(topics = "${kafka.topic.link-updates}", groupId = "bot-group")
    public void handleLinkUpdate(LinkUpdate linkUpdate) {
        System.out.println("Получено обновление ссылки: " + linkUpdate);

        try {
            if (linkUpdate.tgChatIds() != null && !linkUpdate.tgChatIds().isEmpty()) {
                for (Long chatId : linkUpdate.tgChatIds()) {
                    String mode = redisCacheService.getNotificationMode(chatId);

                    if ("immediate".equals(mode)) {
                        sendNotification(chatId, linkUpdate);
                    } else {
                        // Сохраняем уведомление в Redis для дайджеста
                        String notification = "Обновление ссылки:\n"
                            + "URL: " + linkUpdate.url() + "\n"
                            + "Описание: " + linkUpdate.description();
                        redisCacheService.addNotificationToBatch(chatId, notification);
                    }
                }
            } else {
                throw new IllegalArgumentException("Отсутствуют chatId для отправки уведомления.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке обновления: " + e.getMessage());
            throw e;
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
