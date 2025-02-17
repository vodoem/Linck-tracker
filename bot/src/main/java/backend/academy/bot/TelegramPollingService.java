package backend.academy.bot;

import backend.academy.bot.client.TelegramClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class TelegramPollingService {
    private final TelegramBot telegramBot;
    private final BotService botService;
    private int offset = 0;
    private static final Logger logger = LoggerFactory.getLogger(TelegramPollingService.class);

    public TelegramPollingService(TelegramBot telegramBot, BotService botService) {
        this.telegramBot = telegramBot;
        this.botService = botService;
    }

    @Scheduled(fixedRate = 1000)
    public void pollUpdates() {
        GetUpdatesResponse updatesResponse = telegramBot.execute(new GetUpdates().offset(offset).limit(100));
        List<Update> updates = updatesResponse.updates();

        for (Update update : updates) {
            try {
                if (update.message() == null) {
                    logger.info("Пропускаем обновление без сообщения.");
                    continue;
                }

                long chatId = update.message().chat().id();
                String text = update.message().text();

                if (text == null) {
                    System.out.println("Получено сообщение без текста от chatId=" + chatId);
                    continue;
                }

                if (text.startsWith("/")) {
                    String response = botService.handleCommand(text, chatId);
                    telegramBot.execute(new SendMessage(chatId, response));
                } else {
                    String response = botService.handleTextMessage(chatId, text);
                    telegramBot.execute(new SendMessage(chatId, response));
                }

                offset = update.updateId() + 1;
            }catch (Exception e) {
                logger.error("Ошибка при обработке обновления: {}", e.getMessage(), e);
            }
        }
    }
}
