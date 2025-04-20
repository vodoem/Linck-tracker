package backend.academy.bot;

import backend.academy.bot.client.TelegramClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BotInitializer implements CommandLineRunner {

    private final TelegramClient telegramClient;

    public BotInitializer(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void run(String... args) throws Exception {
        telegramClient.setMyCommands();
        System.out.println("Команды бота успешно зарегистрированы.");
    }
}
