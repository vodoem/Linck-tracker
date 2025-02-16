package backend.academy.bot.client;

import backend.academy.bot.BotConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import java.util.List;
import java.util.Map;

@Component
public class TelegramClient {
    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramClient(BotConfig botConfig) {
        this.restTemplate = new RestTemplate();
        this.botToken = botConfig.telegramToken(); // Получаем токен из BotConfig
    }

    // Отправка сообщения
    public void sendMessage(long chatId, String text) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "chat_id", chatId,
            "text", text
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send message");
        }
    }

    // Регистрация команд
    public void setMyCommands() {
        String url = "https://api.telegram.org/bot" + botToken + "/setMyCommands";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        List<Map<String, String>> commands = List.of(
            Map.of("command", "start", "description", "Зарегистрироваться"),
            Map.of("command", "help", "description",  "Показать доступные команды"),
            Map.of("command", "track", "description", "Добавить ссылку для отслеживания"),
            Map.of("command", "untrack", "description", "Удалить ссылку из отслеживания"),
            Map.of("command", "list", "description", "Показать список отслеживаемых ссылок")
        );

        Map<String, Object> body = Map.of("commands", commands);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to set commands");
        }
    }

    // Получение обновлений через Long Polling
    public List<Map<String, Object>> getUpdates(int offset) {
        String url = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + offset;
        System.out.println("Выполняется запрос к Telegram API: " + url);

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        System.out.println("Ответ от Telegram API: " + response.getBody());

        return (List<Map<String, Object>>) response.getBody().get("result");
    }

    public void clearUpdates() {
        String url = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + Integer.MAX_VALUE;
        System.out.println("Очистка старых обновлений...");
        restTemplate.getForEntity(url, Map.class);
    }
}
