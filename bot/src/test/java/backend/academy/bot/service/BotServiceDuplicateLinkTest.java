package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BotServiceDuplicateLinkTest {

    private BotService botService;
    private ScrapperClient scrapperClient;
    private BotStateMachine botStateMachine;
    private CommunicationService communicationService;
    private RedisCacheService redisCacheService;
    private NotificationRouter notificationRouter;

    @BeforeEach
    void setUp() {
        // Мокаем все зависимости
        scrapperClient = Mockito.mock(ScrapperClient.class);
        botStateMachine = Mockito.mock(BotStateMachine.class);
        redisCacheService = Mockito.mock(RedisCacheService.class);
        notificationRouter = Mockito.mock(NotificationRouter.class);

        // Создаем мок для CommunicationService
        communicationService = Mockito.mock(HttpCommunicationService.class);

        // Инициализируем BotService с моками
        botService = new BotService(communicationService, botStateMachine, redisCacheService, notificationRouter);
    }

    @Test
    void testDuplicateLink() {
        // Arrange
        long chatId = 12345L;
        String link = "https://example.com";
        LinkResponse linkResponse = new LinkResponse(1L, link, List.of(), List.of());
        ListLinksResponse listLinksResponse = new ListLinksResponse(List.of(linkResponse), 1);

        // Настройка поведения моков
        when(botStateMachine.getState(chatId)).thenReturn("waiting_for_link");
        when(communicationService.getLinks(chatId)).thenReturn(listLinksResponse);

        // Act
        String response = botService.handleTextMessage(chatId, link);

        // Assert
        assertEquals("Ссылка уже отслеживается.", response);
        verify(communicationService, never()).addLink(anyLong(), anyString(), anyList(), anyList());
        verify(botStateMachine).clearState(chatId);
    }
}
