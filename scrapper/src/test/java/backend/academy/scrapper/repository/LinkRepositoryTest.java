package backend.academy.scrapper.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.model.LinkResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class LinkRepositoryTest {
    @InjectMocks
    private HttpLinkRepository httpLinkRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddLink() {
        // Arrange
        long chatId = 12345L;
        String link = "https://github.com/owner/repo";
        List<String> tags = List.of("tag1", "tag2");
        List<String> filters = List.of("filter1", "filter2");

        // Act
        httpLinkRepository.addLink(chatId, link, tags, filters);

        // Assert
        List<LinkResponse> links = httpLinkRepository.getLinks(chatId);
        assertFalse(links.isEmpty(), "Список ссылок должен быть не пустым");

        LinkResponse addedLink = links.get(0);
        assertEquals(link, addedLink.url(), "Ссылка должна совпадать");
        assertEquals(tags, addedLink.tags(), "Тэги должны совпадать");
        assertEquals(filters, addedLink.filters(), "Фильтры должны совпадать");
    }

    @Test
    void testRemoveLink() {
        // Arrange
        long chatId = 12345L;
        String link = "https://github.com/owner/repo";
        List<String> tags = List.of("tag1", "tag2");
        List<String> filters = List.of("filter1", "filter2");

        // Добавляем ссылку
        httpLinkRepository.addLink(chatId, link, tags, filters);

        // Act
        httpLinkRepository.removeLink(chatId, link);

        // Assert
        List<LinkResponse> links = httpLinkRepository.getLinks(chatId);
        assertTrue(links.isEmpty(), "Список ссылок должен быть пустым");
    }

    @Test
    void testGetLinks() {
        // Arrange
        long chatId = 12345L;
        String link = "https://github.com/owner/repo";
        List<String> tags = List.of("tag1", "tag2");
        List<String> filters = List.of("filter1", "filter2");

        // Добавляем ссылку
        httpLinkRepository.addLink(chatId, link, tags, filters);

        // Act
        List<LinkResponse> links = httpLinkRepository.getLinks(chatId);

        // Assert
        assertFalse(links.isEmpty(), "Список ссылок должен быть не пустым");

        LinkResponse retrievedLink = links.get(0);
        assertEquals(link, retrievedLink.url(), "Ссылка должна совпадать");
        assertEquals(tags, retrievedLink.tags(), "Тэги должны совпадать");
        assertEquals(filters, retrievedLink.filters(), "Фильтры должны совпадать");
    }

    @Test
    void testRegisterChat() {
        // Arrange
        long chatId = 12345L;

        // Act
        httpLinkRepository.registerChat(chatId);

        // Assert
        List<LinkResponse> links = httpLinkRepository.getLinks(chatId);
        assertNotNull(links, "Список ссылок для чата не должен быть null");
        assertTrue(links.isEmpty(), "Список ссылок для нового чата должен быть пустым");
    }

    @Test
    void testGetAllChatIds() {
        // Arrange
        long chatId1 = 12345L;
        long chatId2 = 67890L;

        // Регистрируем чаты
        httpLinkRepository.registerChat(chatId1);
        httpLinkRepository.registerChat(chatId2);

        // Act
        List<Long> chatIds = httpLinkRepository.getAllChatIds();

        // Assert
        assertFalse(chatIds.isEmpty(), "Список chatId должен быть не пустым");
        assertTrue(chatIds.contains(chatId1), "Список chatId должен содержать chatId1");
        assertTrue(chatIds.contains(chatId2), "Список chatId должен содержать chatId2");
    }
}
