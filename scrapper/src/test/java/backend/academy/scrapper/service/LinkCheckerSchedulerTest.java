package backend.academy.scrapper.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.BotClient;
import backend.academy.model.LinkResponse;
import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LinkCheckerSchedulerTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private BotClient botClient;

    @Mock
    private GitHubLinkChecker gitHubLinkChecker; // Мок для GitHub стратегии

    @Mock
    private StackOverflowLinkChecker stackOverflowLinkChecker; // Мок для Stack Overflow стратегии

    @InjectMocks
    private LinkCheckerScheduler linkCheckerScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Настройка поведения стратегий
        when(gitHubLinkChecker.checkForUpdates(anyString())).thenReturn(true);
        when(stackOverflowLinkChecker.checkForUpdates(anyString())).thenReturn(false);

        // Настройка описаний обновлений
        when(gitHubLinkChecker.getUpdateDescription(anyString())).thenReturn("Репозиторий обновлен");
        when(stackOverflowLinkChecker.getUpdateDescription(anyString())).thenReturn("Новый ответ на вопрос");

        List<LinkChecker> linkCheckers = List.of(gitHubLinkChecker, stackOverflowLinkChecker);
        linkCheckerScheduler.setLinkCheckers(linkCheckers);
    }

    @Test
    void testSendUpdatesOnlyToRelevantChats() {
        // Arrange
        long chatId1 = 12345L;
        long chatId2 = 67890L;

        String githubLink = "https://github.com/owner/repo";
        String stackOverflowLink = "https://stackoverflow.com/questions/12345";

        // Мок для получения всех chatId
        when(linkRepository.getAllChatIds()).thenReturn(List.of(chatId1, chatId2));

        // Мок для получения ссылок для каждого чата
        when(linkRepository.getLinks(chatId1))
                .thenReturn(List.of(new LinkResponse(chatId1, githubLink, List.of(), List.of())));
        when(linkRepository.getLinks(chatId2))
                .thenReturn(List.of(new LinkResponse(chatId2, stackOverflowLink, List.of(), List.of())));

        // Act
        linkCheckerScheduler.checkLinks();

        // Assert
        ArgumentCaptor<LinkUpdate> updateCaptor = ArgumentCaptor.forClass(LinkUpdate.class);

        // Убедимся, что уведомление отправлено для chatId1 (GitHub)
        verify(botClient, times(1)).sendUpdate(updateCaptor.capture());
        LinkUpdate sentUpdate = updateCaptor.getValue();
        long chatId = sentUpdate.tgChatIds().get(0);
        assertEquals(chatId1, chatId);
        assertEquals(githubLink, sentUpdate.url());
        assertTrue(sentUpdate.description().contains("Репозиторий обновлен"));

        // Убедимся, что уведомление НЕ отправлено для chatId2 (Stack Overflow)
        verify(botClient, never())
                .sendUpdate(argThat(update ->
                        update.tgChatIds().contains(chatId2) && update.url().equals(stackOverflowLink)));
    }
}
