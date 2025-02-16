package backend.academy.bot;

import backend.academy.bot.client.TelegramClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class TelegramPollingService {
    private final TelegramClient telegramClient;
    private final BotService botService;
    private int offset = 0;

    public TelegramPollingService(TelegramClient telegramClient, BotService botService) {
        this.telegramClient = telegramClient;
        this.botService = botService;
    }

    @Scheduled(fixedRate = 1000)
    public void pollUpdates() {
        // Получаем обновления с текущего offset
        List<Map<String, Object>> updates = telegramClient.getUpdates(offset);

        for (Map<String, Object> update : updates) {
            int updateId = (int) update.get("update_id");

            // Обрабатываем только сообщения (игнорируем другие типы обновлений)
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message != null) {
                long chatId = ((Number) ((Map<String, Object>) message.get("chat")).get("id")).longValue();
                String text = (String) message.get("text");

                if (text == null) {
                    System.out.println("Получено сообщение без текста от chatId=" + chatId);
                    continue;
                }

                // Проверяем, является ли текст командой
                if (text.startsWith("/")) {
                    String response = botService.handleCommand(text, chatId);
                    telegramClient.sendMessage(chatId, response);
                } else {
                    // Обрабатываем текстовые сообщения
                    String response = botService.handleTextMessage(chatId, text);
                    telegramClient.sendMessage(chatId, response);
                }
            }

            // Обновляем offset до следующего update_id
            offset = updateId + 1;
        }
    }
}
