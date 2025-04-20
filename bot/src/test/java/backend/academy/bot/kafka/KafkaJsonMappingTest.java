package backend.academy.bot.kafka;

import backend.academy.bot.AbstractIntegrationTest;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.service.RedisCacheService;
import backend.academy.model.LinkUpdate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ActiveProfiles("test")
@SpringBootTest
public class KafkaJsonMappingTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private TelegramClient telegramClient;

    @Autowired
    private RedisCacheService redisCacheService;

    @Value("${kafka.topic.link-updates}")
    private String linkUpdatesTopic;

    @Captor
    private ArgumentCaptor<Long> chatIdCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Test
    void validJsonShouldBeMappedToDto() throws Exception {
        // Arrange
        Long testChatId = 12345L;
        String testUrl = "https://example.com";
        String testDescription = "Example update";

        redisCacheService.setNotificationMode(testChatId, "immediate");

        LinkUpdate linkUpdate = new LinkUpdate(
            1L,
            testUrl,
            testDescription,
            List.of(testChatId)
        );

        // Act
        kafkaTemplate.send(linkUpdatesTopic, linkUpdate).get();

        // Assert
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(telegramClient).sendMessage(
                chatIdCaptor.capture(),
                messageCaptor.capture()
            );

            assertThat(chatIdCaptor.getValue()).isEqualTo(testChatId);
            assertThat(messageCaptor.getValue())
                .contains(testUrl)
                .contains(testDescription);
        });
    }
}
