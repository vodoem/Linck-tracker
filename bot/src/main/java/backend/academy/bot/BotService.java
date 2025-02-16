package backend.academy.bot;

import backend.academy.bot.repository.LinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotService {
    private final LinkRepository linkRepository;
    private final BotStateMachine botStateMachine;

    public BotService(LinkRepository linkRepository, BotStateMachine botStateMachine) {
        this.linkRepository = linkRepository;
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
                linkRepository.registerChat(chatId);
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
                List<String> links = linkRepository.getLinks(chatId);
                if (links.isEmpty()) {
                    return "У вас нет отслеживаемых ссылок.";
                }
                return "Ваши отслеживаемые ссылки:\n" + String.join("\n", links);
            default:
                return "Неизвестная команда. Используйте /help для просмотра доступных команд.";
        }
    }

    // Обработка текстовых сообщений
    public String handleTextMessage(long chatId, String message) {
        String currentState = botStateMachine.getState(chatId);
        switch (currentState) {
            case "waiting_for_link":
                linkRepository.addLink(chatId, message);
                botStateMachine.clearState(chatId);
                return "Ссылка добавлена в отслеживание.";
            case "waiting_for_untrack_link":
                linkRepository.removeLink(chatId, message);
                botStateMachine.clearState(chatId);
                return "Ссылка удалена из отслеживания.";
            default:
                return "Неизвестное сообщение. Используйте /help для просмотра доступных команд.";
        }
    }
}
