package backend.academy.bot.controller;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.service.RedisCacheService;
import backend.academy.model.LinkUpdate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class UpdateController {

    private final TelegramClient telegramClient;
    private final RedisCacheService redisCacheService;

    public UpdateController(TelegramClient telegramClient, RedisCacheService redisCacheService) {
        this.telegramClient = telegramClient;
        this.redisCacheService = redisCacheService;
    }

    @PostMapping
    public ResponseEntity<Void> handleUpdate(@RequestBody @Valid LinkUpdate linkUpdate) {
        // Логика обработки обновления
        System.out.println("Получено обновление: " + linkUpdate);

        // Отправка уведомлений в чаты
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
            System.out.println("Нет chatId для отправки уведомления.");
        }

        return ResponseEntity.ok().build();
    }

    private void sendNotification(Long chatId, LinkUpdate update) {
        // Отправка уведомлений в чаты
        if (update.tgChatIds() != null && !update.tgChatIds().isEmpty()) {
            String message =
                    "Обновление ссылки:\n" + "URL: " + update.url() + "\n" + "Описание: " + update.description();
            telegramClient.sendMessage(chatId, message);
        } else {
            System.out.println("Нет chatId для отправки уведомления.");
        }
    }
}
