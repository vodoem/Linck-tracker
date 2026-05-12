package backend.academy.bot.client;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import org.springframework.stereotype.Component;

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
                new BotCommand("list", "Показать список отслеживаемых ссылок"),
                new BotCommand("addtags", "Добавить теги к ссылке"),
                new BotCommand("removetag", "Удалить тег из ссылки"),
                new BotCommand("listtags", "Показать все теги для ссылки"),
                new BotCommand("filterbytag", "Показать ссылки с указанным тегом"),
                new BotCommand("settings", "Показать текущие настройки уведомлений"),
                new BotCommand("setmode", "Выбрать режим уведомлений (immediate/digest)")));
        if (!response.isOk()) {
            throw new RuntimeException("Failed to set commands: " + response.description());
        }
    }
}
