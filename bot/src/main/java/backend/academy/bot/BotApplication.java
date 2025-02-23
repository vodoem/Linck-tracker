package backend.academy.bot;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.config.BotConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class})
@EnableScheduling
public class BotApplication implements CommandLineRunner {
    private final TelegramClient telegramClient;

    public BotApplication(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        telegramClient.setMyCommands();
        System.out.println("Команды бота успешно зарегистрированы.");
    }
}
