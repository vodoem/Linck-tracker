package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.client.GitHubClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GitHubLinkCheckerTest {

    private GitHubClient mockGitHubClient;
    private GitHubLinkChecker gitHubLinkChecker;

    @BeforeEach
    public void setUp() {
        // Создаем мок для GitHubClient
        mockGitHubClient = Mockito.mock(GitHubClient.class);
        gitHubLinkChecker = new GitHubLinkChecker(mockGitHubClient);
    }

    @Test
    public void testGetUpdateDescription() throws Exception {
        // Arrange: создаем фиктивный JSON-ответ от GitHub API
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse =
                """
            {
                "title": "Fix bug in login functionality",
                "user": {
                    "login": "vodoem"
                },
                "created_at": "2025-03-20T10:00:00Z",
                "body": "Fixed the issue where users couldn't log in due to incorrect credentials."
            }
        """;
        JsonNode fakeIssue = objectMapper.readTree(jsonResponse);

        // Мокируем метод getLatestIssueOrPR, чтобы он возвращал фиктивный ответ
        Mockito.when(mockGitHubClient.getLatestIssueOrPR("vodoem", "bot-test")).thenReturn(fakeIssue);

        // Act: вызываем метод getUpdateDescription
        String link = "https://github.com/vodoem/bot-test";
        String description = gitHubLinkChecker.getUpdateDescription(link);

        // Assert: проверяем, что описание содержит ожидаемые данные
        assertTrue(description.contains("Новый PR/Issue: Fix bug in login functionality"));
        assertTrue(description.contains("Автор: vodoem"));
        assertTrue(description.contains("Дата создания: 2025-03-20T10:00:00Z"));
        assertTrue(description.contains("Превью описания: Fixed the issue where users"));
    }
}
