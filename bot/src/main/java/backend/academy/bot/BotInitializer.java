package backend.academy.bot;

import backend.academy.bot.client.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BotInitializer implements CommandLineRunner {

    private final TelegramClient telegramClient;
    private static final Logger logger = LoggerFactory.getLogger(BotInitializer.class);

    public BotInitializer(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void run(String... args) throws Exception {
        telegramClient.setMyCommands();
        logger.info("Команды бота успешно зарегистрированы.");
    }
}
