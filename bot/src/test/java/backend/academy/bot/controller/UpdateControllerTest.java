package backend.academy.bot.controller;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.service.RedisCacheService;
import backend.academy.model.LinkUpdate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UpdateControllerTest {
    private final TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
    private final RedisCacheService redisCacheService = Mockito.mock(RedisCacheService.class);
    private final UpdateController updateController = new UpdateController(telegramClient, redisCacheService);

    @Test
    void shouldSendImmediateNotificationWhenNotificationModeIsNotConfigured() {
        long chatId = 1L;
        LinkUpdate update = new LinkUpdate(chatId, "https://github.com/example/repo", "new issue", List.of(chatId));
        when(redisCacheService.getNotificationMode(chatId)).thenReturn(null);

        updateController.handleUpdate(update);

        verify(telegramClient).sendMessage(eq(chatId), contains(update.url()));
        verify(redisCacheService, never()).addNotificationToBatch(eq(chatId), Mockito.anyString());
    }
}
