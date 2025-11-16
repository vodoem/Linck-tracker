package backend.academy.bot.service;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.user.WebUserRepository;
import backend.academy.bot.web.ChatSessionService;
import backend.academy.model.LinkUpdate;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationRouter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRouter.class);

    private final TelegramClient telegramClient;
    private final RedisCacheService redisCacheService;
    private final ChatSessionService chatSessionService;
    private final WebUserRepository webUserRepository;

    public NotificationRouter(
            TelegramClient telegramClient,
            RedisCacheService redisCacheService,
            ChatSessionService chatSessionService,
            WebUserRepository webUserRepository) {
        this.telegramClient = telegramClient;
        this.redisCacheService = redisCacheService;
        this.chatSessionService = chatSessionService;
        this.webUserRepository = webUserRepository;
    }

    public void dispatchUpdate(LinkUpdate update) {
        List<Long> chatIds = update.tgChatIds();
        if (chatIds == null || chatIds.isEmpty()) {
            LOGGER.warn("Получено обновление {} без chatId", update.id());
            return;
        }

        String message = formatLinkUpdate(update);
        chatIds.stream().filter(Objects::nonNull).forEach(chatId -> routeSingleMessage(chatId, message));
    }

    public void dispatchDigest(long chatId, String digestMessage) {
        if (isWebUser(chatId)) {
            chatSessionService.appendBotMessage(chatId, digestMessage);
        } else {
            sendToTelegram(chatId, digestMessage);
        }
    }

    private void routeSingleMessage(long chatId, String message) {
        String mode = redisCacheService.getNotificationMode(chatId);
        boolean digestMode = "digest".equalsIgnoreCase(mode);

        if (isWebUser(chatId)) {
            if (digestMode) {
                redisCacheService.addNotificationToBatch(chatId, message);
            } else {
                chatSessionService.appendBotMessage(chatId, message);
            }
            return;
        }

        if (digestMode) {
            redisCacheService.addNotificationToBatch(chatId, message);
        } else {
            sendToTelegram(chatId, message);
        }
    }

    private boolean isWebUser(long chatId) {
        return webUserRepository.existsById(chatId);
    }

    private void sendToTelegram(long chatId, String message) {
        try {
            telegramClient.sendMessage(chatId, message);
        } catch (RuntimeException ex) {
            LOGGER.warn("Не удалось отправить сообщение в Telegram-чат {}: {}", chatId, ex.getMessage());
        }
    }

    private String formatLinkUpdate(LinkUpdate update) {
        return "Обновление ссылки:\n" + "URL: " + update.url() + "\n" + "Описание: " + update.description();
    }
}
