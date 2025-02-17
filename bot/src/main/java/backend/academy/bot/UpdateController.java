package backend.academy.bot;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.model.LinkUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class UpdateController {

    private final TelegramClient telegramClient;

    public UpdateController(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @PostMapping
    public ResponseEntity<Void> handleUpdate(@RequestBody LinkUpdate linkUpdate) {
        // Логика обработки обновления
        System.out.println("Получено обновление: " + linkUpdate);

        // Отправка уведомлений в чаты
        if (linkUpdate.tgChatIds() != null && !linkUpdate.tgChatIds().isEmpty()) {
            for (Long chatId : linkUpdate.tgChatIds()) {
                String message = "Обновление ссылки:\n"
                    + "URL: " + linkUpdate.url() + "\n"
                    + "Описание: " + linkUpdate.description();
                telegramClient.sendMessage(chatId, message);
            }
        } else {
            System.out.println("Нет chatId для отправки уведомления.");
        }

        return ResponseEntity.ok().build();
    }
}
