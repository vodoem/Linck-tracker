package backend.academy.bot.service;

import backend.academy.bot.client.TelegramClient;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BotService {
    private final CommunicationService communicationService;
    private final BotStateMachine botStateMachine;
    private final RedisCacheService redisCacheService;
    private final TelegramClient telegramClient;
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?$", Pattern.CASE_INSENSITIVE);

    public BotService(
            CommunicationService communicationService,
            BotStateMachine botStateMachine,
            RedisCacheService redisCacheService,
            TelegramClient telegramClient) {
        this.communicationService = communicationService;
        this.botStateMachine = botStateMachine;
        this.redisCacheService = redisCacheService;
        this.telegramClient = telegramClient;
    }
    // Обработка команд
    public String handleCommand(String command, long chatId) {
        return handleCommandWithKeyboard(command, chatId).text();
    }

    public BotReply handleCommandWithKeyboard(String command, long chatId) {
        if (command == null) {
            System.out.println("Текст сообщения равен null. Пропускаем обработку.");
            return mainReply("Извините, я не могу обработать это сообщение.");
        }
        switch (command) {
            case "/start":
                communicationService.registerChat(chatId);
                redisCacheService.setNotificationMode(chatId, "immediate");
                return mainReply(
                        "Добро пожаловать! Я добавил удобные кнопки ниже — начните с добавления ссылки или посмотрите список отслеживаемого.");
            case "/help":
                return mainReply(
                        """
                    Главное меню вынесено в кнопки:
                    ➕ Добавить ссылку — основной сценарий отслеживания.
                    📋 Мои ссылки — быстрый просмотр всех ссылок.
                    ➖ Удалить ссылку — остановить отслеживание.
                    🏷 Теги — добавить, удалить, посмотреть теги и фильтровать ссылки.
                    🔔 Уведомления — выбрать моментальные уведомления или ежедневный дайджест.

                    Команды через / по-прежнему работают, если они вам удобны.
                    """);
            case "/track":
                botStateMachine.setState(chatId, "waiting_for_link");
                return inputReply("Отправьте ссылку, которую нужно отслеживать.");
            case "/untrack":
                botStateMachine.setState(chatId, "waiting_for_untrack_link");
                return inputReply("Отправьте ссылку, которую нужно удалить из отслеживания.");
            case "/list":
                ListLinksResponse linksResponse = communicationService.getLinks(chatId);
                if (linksResponse.links().isEmpty()) {
                    return mainReply("У вас пока нет отслеживаемых ссылок. Нажмите «➕ Добавить ссылку», чтобы начать.");
                }
                return mainReply("Ваши отслеживаемые ссылки:\n"
                        + linksResponse.links().stream().map(LinkResponse::url).collect(Collectors.joining("\n")));
            case "/addtags":
                botStateMachine.setState(chatId, "waiting_for_addtags");
                return inputReply("Отправьте URL и теги через пробел. Например: https://example.com java backend");
            case "/removetag":
                botStateMachine.setState(chatId, "waiting_for_removetag");
                return inputReply("Отправьте URL и имя тега через пробел. Например: https://example.com java");
            case "/listtags":
                botStateMachine.setState(chatId, "waiting_for_listtags");
                return inputReply("Отправьте URL ссылки, для которой нужно показать теги.");
            case "/filterbytag":
                botStateMachine.setState(chatId, "waiting_for_filterbytag");
                return inputReply("Отправьте имя тега, по которому нужно найти ссылки.");
            case "/settings":
                String mode = redisCacheService.getNotificationMode(chatId);
                return settingsReply("Текущий режим уведомлений: " + formatNotificationMode(mode)
                        + "\nВыберите удобный режим кнопкой ниже.");

            case "/setmode immediate":
                redisCacheService.setNotificationMode(chatId, "immediate");
                return mainReply("Режим уведомлений установлен: сразу.");

            case "/setmode digest":
                redisCacheService.setNotificationMode(chatId, "digest");
                return mainReply("Режим уведомлений установлен: дайджест раз в сутки.");
            default:
                return mainReply("Неизвестная команда. Используйте /help для просмотра доступных команд.");
        }
    }

    // Обработка текстовых сообщений
    public String handleTextMessage(long chatId, String message) {
        return handleTextMessageWithKeyboard(chatId, message).text();
    }

    public BotReply handleTextMessageWithKeyboard(long chatId, String message) {
        if (message == null) {
            return mainReply("Извините, я не могу обработать это сообщение.");
        }
        if (BotKeyboards.CANCEL.equals(message)) {
            botStateMachine.clearState(chatId);
            return mainReply("Действие отменено. Выберите следующий шаг в меню.");
        }

        String currentState = botStateMachine.getState(chatId);
        if (currentState == null || currentState.isEmpty()) {
            return handleMenuMessage(message, chatId);
        }

        if (BotKeyboards.SKIP.equals(message)) {
            message = "-";
        }

        switch (currentState) {
            case "waiting_for_link":
                if (!isValidUrl(message)) {
                    botStateMachine.clearState(chatId);
                    return mainReply(
                            "Некорректная ссылка. Пожалуйста, нажмите «➕ Добавить ссылку» и отправьте корректный URL.");
                }

                // Получаем текущие ссылки для чата
                String trackedLinkCandidate = message;
                ListLinksResponse linksResponse = communicationService.getLinks(chatId);
                boolean isLinkAlreadyTracked = linksResponse.links().stream()
                        .anyMatch(link -> link.url().equals(trackedLinkCandidate));

                if (isLinkAlreadyTracked) {
                    botStateMachine.clearState(chatId);
                    return mainReply("Ссылка уже отслеживается.");
                }

                // Добавляем ссылку
                botStateMachine.setPendingLink(chatId, message);
                botStateMachine.setState(chatId, "waiting_for_tags");
                return optionalInputReply("Введите теги через пробел или нажмите «⏭ Пропустить», если теги не нужны.");
            case "waiting_for_tags":
                List<String> tags = Arrays.asList(message.trim().split("\\s+"));
                if (tags.size() == 1 && "-".equals(tags.get(0))) {
                    botStateMachine.setPendingTags(chatId, Collections.emptyList());
                } else {
                    botStateMachine.setPendingTags(chatId, tags);
                }
                botStateMachine.setState(chatId, "waiting_for_filters");
                return optionalInputReply(
                        "Настройте фильтры (например, user:dummy type:comment) или нажмите «⏭ Пропустить».");

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

                communicationService.addLink(chatId, link, pendingTags, pendingFilters);
                botStateMachine.clearState(chatId);
                return mainReply(
                        "Ссылка успешно добавлена с тегами: " + pendingTags + " и фильтрами: " + pendingFilters);
            case "waiting_for_untrack_link":
                communicationService.removeLink(chatId, message);
                botStateMachine.clearState(chatId);
                return mainReply("Ссылка удалена из отслеживания.");

            case "waiting_for_addtags":
                String[] parts = message.trim().split("\\s+");
                if (parts.length < 2) {
                    return inputReply("Нужно отправить URL и хотя бы один тег. Например: https://example.com java");
                }
                String url = parts[0];
                List<String> tagsForUrl = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                communicationService.addTags(chatId, url, tagsForUrl);
                botStateMachine.clearState(chatId);
                return mainReply("Теги успешно добавлены.");

            case "waiting_for_removetag":
                String[] removeParts = message.trim().split("\\s+");
                if (removeParts.length < 2) {
                    return inputReply("Нужно отправить URL и имя тега. Например: https://example.com java");
                }
                String removeUrl = removeParts[0];
                String tagName = removeParts[1];
                communicationService.removeTag(chatId, removeUrl, tagName);
                botStateMachine.clearState(chatId);
                return mainReply("Тег успешно удален.");

            case "waiting_for_listtags":
                List<String> tagsList = communicationService.getTagsForLink(chatId, message);
                botStateMachine.clearState(chatId);
                return mainReply("Теги для ссылки: " + String.join(", ", tagsList));

            case "waiting_for_filterbytag":
                List<LinkResponse> filteredLinks = communicationService.getLinksByTag(chatId, message);
                botStateMachine.clearState(chatId);
                return mainReply("Ссылки с тегом '" + message + "':\n"
                        + filteredLinks.stream().map(LinkResponse::url).collect(Collectors.joining("\n")));

            default:
                return mainReply("Неизвестное сообщение. Выберите действие кнопкой ниже или используйте /help.");
        }
    }

    private BotReply handleMenuMessage(String message, long chatId) {
        return switch (message) {
            case BotKeyboards.TRACK -> handleCommandWithKeyboard("/track", chatId);
            case BotKeyboards.LIST -> handleCommandWithKeyboard("/list", chatId);
            case BotKeyboards.UNTRACK -> handleCommandWithKeyboard("/untrack", chatId);
            case BotKeyboards.ADD_TAGS -> handleCommandWithKeyboard("/addtags", chatId);
            case BotKeyboards.REMOVE_TAG -> handleCommandWithKeyboard("/removetag", chatId);
            case BotKeyboards.LIST_TAGS -> handleCommandWithKeyboard("/listtags", chatId);
            case BotKeyboards.FILTER_BY_TAG -> handleCommandWithKeyboard("/filterbytag", chatId);
            case BotKeyboards.HELP, BotKeyboards.BACK -> handleCommandWithKeyboard("/help", chatId);
            case BotKeyboards.SETTINGS -> handleCommandWithKeyboard("/settings", chatId);
            case BotKeyboards.IMMEDIATE_MODE -> handleCommandWithKeyboard("/setmode immediate", chatId);
            case BotKeyboards.DIGEST_MODE -> handleCommandWithKeyboard("/setmode digest", chatId);
            case BotKeyboards.TAGS -> tagsReply("Выберите действие с тегами.");
            default -> {
                if (message != null && message.startsWith("/")) {
                    yield handleCommandWithKeyboard(message, chatId);
                }
                yield mainReply("Неизвестное сообщение. Выберите действие кнопкой ниже или используйте /help.");
            }
        };
    }

    private BotReply mainReply(String text) {
        return new BotReply(text, BotKeyboards.mainMenu());
    }

    private BotReply tagsReply(String text) {
        return new BotReply(text, BotKeyboards.tagsMenu());
    }

    private BotReply settingsReply(String text) {
        return new BotReply(text, BotKeyboards.settingsMenu());
    }

    private BotReply inputReply(String text) {
        return new BotReply(text, BotKeyboards.inputMenu());
    }

    private BotReply optionalInputReply(String text) {
        return new BotReply(text, BotKeyboards.optionalInputMenu());
    }

    private String formatNotificationMode(String mode) {
        return switch (mode == null ? "immediate" : mode) {
            case "digest" -> "дайджест раз в сутки";
            case "immediate" -> "сразу";
            default -> mode;
        };
    }

    @Scheduled(cron = "${app.digest}") // Например, "0 0 10 * * ?" (каждый день в 10:00)
    public void sendDailyDigest() {
        List<Long> chatIds = redisCacheService.getAllChatIdsWithNotifications();

        for (long chatId : chatIds) {
            String mode = redisCacheService.getNotificationMode(chatId);
            if (!"digest".equals(mode)) {
                continue; // Пропускаем чаты, которые не выбрали режим дайджеста
            }

            List<String> notifications = redisCacheService.getNotificationsFromBatch(chatId);
            if (notifications.isEmpty()) {
                continue; // Нет уведомлений для отправки
            }

            // Формируем дайджест
            String digestMessage = "Дайджест обновлений:\n" + String.join("\n\n", notifications);
            telegramClient.sendMessage(chatId, digestMessage);

            // Очищаем батч
            redisCacheService.clearNotificationBatch(chatId);
        }
    }

    public boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
}
