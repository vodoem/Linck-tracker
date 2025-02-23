package backend.academy.bot.service;


import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.LinkResponse;
import backend.academy.bot.model.ListLinksResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BotService {
    private final ScrapperClient scrapperClient;
    private final BotStateMachine botStateMachine;
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?$",
        Pattern.CASE_INSENSITIVE
    );

    public BotService(ScrapperClient scrapperClient, BotStateMachine botStateMachine) {
        this.scrapperClient = scrapperClient;
        this.botStateMachine = botStateMachine;
    }
    // Обработка команд
    public String handleCommand(String command, long chatId) {
        if (command == null) {
            System.out.println("Текст сообщения равен null. Пропускаем обработку.");
            return "Извините, я не могу обработать это сообщение.";
        }
        switch (command) {
            case "/start":
                scrapperClient.registerChat(chatId);
                return "Добро пожаловать! Используйте /help для просмотра доступных команд.";
            case "/help":
                return """
                    Доступные команды:
                    /start - регистрация пользователя
                    /track - добавить ссылку для отслеживания
                    /untrack - удалить ссылку из отслеживания
                    /list - показать список отслеживаемых ссылок
                    """;
            case "/track":
                botStateMachine.setState(chatId, "waiting_for_link");
                return "Введите ссылку для отслеживания.";
            case "/untrack":
                botStateMachine.setState(chatId, "waiting_for_untrack_link");
                return "Введите ссылку для удаления.";
            case "/list":
                ListLinksResponse linksResponse = scrapperClient.getLinks(chatId);
                if (linksResponse.links().isEmpty()) {
                    return "У вас нет отслеживаемых ссылок.";
                }
                return "Ваши отслеживаемые ссылки:\n" +
                    linksResponse.links().stream()
                        .map(LinkResponse::url)
                        .collect(Collectors.joining("\n"));
            default:
                return "Неизвестная команда. Используйте /help для просмотра доступных команд.";
        }
    }

    // Обработка текстовых сообщений
    public String handleTextMessage(long chatId, String message) {
        String currentState = botStateMachine.getState(chatId);
        switch (currentState) {
            case "waiting_for_link":
                if (!isValidUrl(message)) {
                    botStateMachine.clearState(chatId);
                    return "Некорректная ссылка. Пожалуйста, введите корректный URL.";
                }

                // Получаем текущие ссылки для чата
                ListLinksResponse linksResponse = scrapperClient.getLinks(chatId);
                boolean isLinkAlreadyTracked = linksResponse.links().stream()
                    .anyMatch(link -> link.url().equals(message));

                if (isLinkAlreadyTracked) {
                    botStateMachine.clearState(chatId);
                    return "Ссылка уже отслеживается.";
                }

                // Добавляем ссылку
                scrapperClient.addLink(chatId, message, List.of(), List.of());
                botStateMachine.clearState(chatId);
                return "Ссылка добавлена в отслеживание.";

            case "waiting_for_untrack_link":
                scrapperClient.removeLink(chatId, message);
                botStateMachine.clearState(chatId);
                return "Ссылка удалена из отслеживания.";

            default:
                return "Неизвестное сообщение. Используйте /help для просмотра доступных команд.";
        }
    }
    public boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
}
