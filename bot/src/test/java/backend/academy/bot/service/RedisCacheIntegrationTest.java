package backend.academy.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.AbstractIntegrationTest;
import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(properties = {"app.message-transport=HTTP"})
public class RedisCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private RedisCacheService redisCacheService;

    @MockitoBean
    private ScrapperClient scrapperClient; // Мок для HTTP-клиента

    @MockitoBean
    private TelegramClient telegramClient;

    @Value("${app.message-transport}")
    private String messageTransport;

    @Test
    void shouldCacheLinksOnFirstRequest() throws Exception {
        // Arrange: Подготовка мока для получения данных из ScrapperClient
        long chatId = 1L;
        List<LinkResponse> links = List.of(
                new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1")),
                new LinkResponse(2L, "https://example.org", List.of("tag2"), List.of("filter2")));
        ListLinksResponse response = new ListLinksResponse(links, links.size());

        if ("HTTP".equals(messageTransport)) {
            when(scrapperClient.getLinks(chatId)).thenReturn(response);
        }

        // Act: Первый вызов getLinks
        ListLinksResponse firstResponse = communicationService.getLinks(chatId);

        // Assert: Данные получены из основного источника
        assertThat(firstResponse.links()).hasSize(2);
        if ("HTTP".equals(messageTransport)) {
            verify(scrapperClient, times(1)).getLinks(chatId);
        }

        // Act: Второй вызов getLinks
        ListLinksResponse secondResponse = communicationService.getLinks(chatId);

        // Assert: Данные получены из кэша
        assertThat(secondResponse.links()).hasSize(2);
        if ("HTTP".equals(messageTransport)) {
            verify(scrapperClient, times(1)).getLinks(chatId); // Проверяем, что ScrapperClient вызван только один раз
        }

        redisCacheService.invalidateCache(chatId);
    }

    @Test
    void shouldInvalidateCacheOnDataChange() throws Exception {
        // Arrange: Подготовка мока для получения данных из ScrapperClient
        long chatId = 1L;
        List<LinkResponse> initialLinks =
                List.of(new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1")));
        ListLinksResponse initialResponse = new ListLinksResponse(initialLinks, initialLinks.size());

        List<LinkResponse> updatedLinks = List.of(
                new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1")),
                new LinkResponse(2L, "https://example.org", List.of("tag2"), List.of("filter2")));
        ListLinksResponse updatedResponse = new ListLinksResponse(updatedLinks, updatedLinks.size());

        if ("HTTP".equals(messageTransport)) {
            when(scrapperClient.getLinks(chatId))
                    .thenReturn(initialResponse) // Первый ответ
                    .thenReturn(updatedResponse); // Второй ответ после инвалидации
        }

        // Act: Первый вызов getLinks
        ListLinksResponse firstResponse = communicationService.getLinks(chatId);

        // Assert: Данные получены из основного источника
        assertThat(firstResponse.links()).hasSize(1);
        if ("HTTP".equals(messageTransport)) {
            verify(scrapperClient, times(1)).getLinks(chatId);
        }

        // Act: Изменение данных (инвалидация кэша)
        communicationService.addLink(chatId, "https://example.org", List.of("tag2"), List.of("filter2"));

        // Act: Второй вызов getLinks
        ListLinksResponse secondResponse = communicationService.getLinks(chatId);

        // Assert: Данные получены из основного источника (кэш был очищен)
        assertThat(secondResponse.links()).hasSize(2);
        if ("HTTP".equals(messageTransport)) {
            verify(scrapperClient, times(2)).getLinks(chatId); // Проверяем, что ScrapperClient вызван второй раз
        }

        redisCacheService.invalidateCache(chatId);
    }
}
