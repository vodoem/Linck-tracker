package backend.academy.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.model.LinkResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("orm-test") // Активация профиля ORM
@Import(RepoConfig.class)
public class OrmLinkRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private LinkRepository linkRepository;

    @Test
    void testCRUDOperations() {
        // Arrange
        long chatId = 67890L;
        String link = "https://stackoverflow.com/questions/12345";

        linkRepository.registerChat(chatId);

        // Act: добавляем ссылку
        linkRepository.addLink(chatId, link, List.of(), List.of());

        // Assert: проверяем, что ссылка добавлена
        List<LinkResponse> links = linkRepository.getLinks(chatId, 0, 10);
        assertThat(links).isNotEmpty();
        assertThat(links.get(0).url()).isEqualTo(link);

        // Act: удаляем ссылку
        linkRepository.removeLink(chatId, link);

        // Assert: проверяем, что ссылка удалена
        links = linkRepository.getLinks(chatId, 0, 10);
        assertThat(links).isEmpty();
    }
}
