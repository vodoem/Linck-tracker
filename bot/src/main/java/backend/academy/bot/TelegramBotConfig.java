package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {
    private final BotConfig botConfig;

    public TelegramBotConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botConfig.telegramToken());
    }
}
