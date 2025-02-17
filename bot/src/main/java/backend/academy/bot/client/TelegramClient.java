package backend.academy.bot.client;

import backend.academy.bot.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class TelegramClient {
    private final TelegramBot telegramBot;

    public TelegramClient(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    // Отправка сообщения
    public void sendMessage(long chatId, String text) {
        BaseResponse response = telegramBot.execute(new SendMessage(chatId, text));
        if (!response.isOk()) {
            throw new RuntimeException("Failed to send message: " + response.description());
        }
    }

    // Регистрация команд
    public void setMyCommands() {
        BaseResponse response = telegramBot.execute(new SetMyCommands(
                new BotCommand("start", "Зарегистрироваться"),
                new BotCommand("help", "Показать доступные команды"),
                new BotCommand("track", "Добавить ссылку для отслеживания"),
                new BotCommand("untrack", "Удалить ссылку из отслеживания"),
                new BotCommand("list", "Показать список отслеживаемых ссылок")
        ));
        if (!response.isOk()) {
            throw new RuntimeException("Failed to set commands: " + response.description());
        }
    }
}
