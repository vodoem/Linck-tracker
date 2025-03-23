package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BotService {
    private final ScrapperClient scrapperClient;
    private final BotStateMachine botStateMachine;
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?$", Pattern.CASE_INSENSITIVE);

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
                    /addtags <url> <tag1> <tag2> ... — добавить теги к ссылке.
                    /removetag <url> <tag> — удалить тег из ссылки.
                    /listtags <url> — показать все теги для ссылки.
                    /filterbytag <tag> — показать только ссылки с указанным тегом.
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
                return "Ваши отслеживаемые ссылки:\n"
                        + linksResponse.links().stream().map(LinkResponse::url).collect(Collectors.joining("\n"));
            case "/addtags":
                botStateMachine.setState(chatId, "waiting_for_addtags");
                return "Введите URL и теги через пробел.";
            case "/removetag":
                botStateMachine.setState(chatId, "waiting_for_removetag");
                return "Введите URL и имя тега через пробел.";
            case "/listtags":
                botStateMachine.setState(chatId, "waiting_for_listtags");
                return "Введите URL для просмотра тегов.";
            case "/filterbytag":
                botStateMachine.setState(chatId, "waiting_for_filterbytag");
                return "Введите имя тега для фильтрации ссылок.";
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
                botStateMachine.setPendingLink(chatId, message);
                botStateMachine.setState(chatId, "waiting_for_tags");
                return "Введите тэги (через пробел). Если тэги не нужны, отправьте -";
            case "waiting_for_tags":
                List<String> tags = Arrays.asList(message.trim().split("\\s+"));
                if (tags.size() == 1 && "-".equals(tags.get(0))) {
                    botStateMachine.setPendingTags(chatId, Collections.emptyList());
                } else {
                    botStateMachine.setPendingTags(chatId, tags);
                }
                botStateMachine.setState(chatId, "waiting_for_filters");
                return "Настройте фильтры (например, user:dummy type:comment). Если фильтры не нужны, отправьте -";

            case "waiting_for_filters":
                List<String> filters = Arrays.asList(message.trim().split("\\s+"));
                if (filters.size() == 1 && "-".equals(filters.get(0))) {
                    botStateMachine.setPendingFilters(chatId, Collections.emptyList());
                } else {
                    botStateMachine.setPendingFilters(chatId, filters);
                }

                // Добавляем ссылку в репозиторий
                String link = botStateMachine.getPendingLink(chatId);
                List<String> pendingTags = botStateMachine.getPendingTags(chatId);
                List<String> pendingFilters = botStateMachine.getPendingFilters(chatId);

                scrapperClient.addLink(chatId, link, pendingTags, pendingFilters);
                botStateMachine.clearState(chatId);
                return "Ссылка успешно добавлена с тэгами: " + pendingTags + " и фильтрами: " + pendingFilters;
            case "waiting_for_untrack_link":
                scrapperClient.removeLink(chatId, message);
                botStateMachine.clearState(chatId);
                return "Ссылка удалена из отслеживания.";

            case "waiting_for_addtags":
                String[] parts = message.split(" ");
                String url = parts[0];
                List<String> tagsForUrl = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                scrapperClient.addTags(chatId, url, tagsForUrl);
                botStateMachine.clearState(chatId);
                return "Теги успешно добавлены.";

            case "waiting_for_removetag":
                String[] removeParts = message.split(" ");
                String removeUrl = removeParts[0];
                String tagName = removeParts[1];
                scrapperClient.removeTag(chatId, removeUrl, tagName);
                botStateMachine.clearState(chatId);
                return "Тег успешно удален.";

            case "waiting_for_listtags":
                List<String> tagsList = scrapperClient.getTagsForLink(chatId, message);
                botStateMachine.clearState(chatId);
                return "Теги для ссылки: " + String.join(", ", tagsList);

            case "waiting_for_filterbytag":
                List<LinkResponse> filteredLinks = scrapperClient.getLinksByTag(chatId, message);
                botStateMachine.clearState(chatId);
                return "Ссылки с тегом '" + message + "':\n"
                        + filteredLinks.stream().map(LinkResponse::url).collect(Collectors.joining("\n"));

            default:
                return "Неизвестное сообщение. Используйте /help для просмотра доступных команд.";
        }
    }

    public boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
}
