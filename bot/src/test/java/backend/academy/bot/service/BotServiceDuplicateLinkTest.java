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

public class BotServiceDuplicateLinkTest {
    private BotService botService;
    private ScrapperClient scrapperClient;
    private BotStateMachine botStateMachine;

    @BeforeEach
    void setUp() {
        scrapperClient = Mockito.mock(ScrapperClient.class);
        botStateMachine = Mockito.mock(BotStateMachine.class);
        botService = new BotService(scrapperClient, botStateMachine);
    }

    @Test
    void testDuplicateLink() {
        // Arrange
        long chatId = 12345L;
        String link = "https://example.com";
        LinkResponse linkResponse = new LinkResponse(1L, link, List.of(), List.of());
        ListLinksResponse listLinksResponse = new ListLinksResponse(List.of(linkResponse), 1);
        when(scrapperClient.getLinks(chatId)).thenReturn(listLinksResponse);
        when(botStateMachine.getState(chatId)).thenReturn("waiting_for_link");

        // Act
        String response = botService.handleTextMessage(chatId, link);

        // Assert
        assertEquals("Ссылка уже отслеживается.", response);
        verify(scrapperClient, never()).addLink(anyLong(), anyString(), anyList(), anyList());
        verify(botStateMachine).clearState(chatId);
    }
}
