package backend.academy.bot;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.service.BotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatMemberUpdated;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TelegramPollingService {
    private final TelegramBot telegramBot;
    private final BotService botService;
    private final ScrapperClient scrapperClient;
    private int offset = 0;

    public TelegramPollingService(TelegramBot telegramBot, BotService botService, ScrapperClient scrapperClient) {
        this.telegramBot = telegramBot;
        this.botService = botService;
        this.scrapperClient = scrapperClient;
    }

    @Scheduled(fixedRate = 1000)
    public void pollUpdates() {
        GetUpdatesResponse updatesResponse =
                telegramBot.execute(new GetUpdates().offset(offset).limit(100));
        List<Update> updates = updatesResponse.updates();

        for (Update update : updates) {

            if (update.message() != null) {
                // Обработка текстовых сообщений
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
            } else if (update.myChatMember() != null) {
                // Обработка изменения статуса чата
                handleMyChatMember(update.myChatMember());
            }

            offset = update.updateId() + 1;
        }
    }

    private void handleMyChatMember(ChatMemberUpdated myChatMember) {
        long chatId = myChatMember.chat().id();
        ChatMember newChatMember = myChatMember.newChatMember();

        // Проверяем, был ли бот удален из чата
        if ("left".equals(newChatMember.status().toString())
                || "kicked".equals(newChatMember.status().toString())) {
            scrapperClient.deleteChat(chatId); // Удаляем чат из Scrapper
        }
    }
}
