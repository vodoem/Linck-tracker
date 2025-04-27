package backend.academy.bot.Json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import backend.academy.bot.AbstractIntegrationTest;
import backend.academy.bot.BotInitializer;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.service.RedisCacheService;
import backend.academy.model.LinkUpdate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaJsonMappingTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private TelegramClient telegramClient;

    @MockitoBean
    private BotInitializer botInitializer;

    @Autowired
    private RedisCacheService redisCacheService;

    @Value("${kafka.topic.link-updates}")
    private String linkUpdatesTopic;

    @Value("${kafka.topic.dead-letter-queue}")
    private String dlqTopic;

    @Captor
    private ArgumentCaptor<Long> chatIdCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @BeforeEach
    void clearRedis() {
        redisCacheService.clearAllNotificationData();
    }

    @AfterEach
    void clearRedisAfter() {
        redisCacheService.clearAllNotificationData();
    }

    @Test
    void validJsonShouldBeMappedToDto() throws Exception {
        // Arrange
        Long testChatId = 12345L;
        String testUrl = "https://example.com";
        String testDescription = "Example update";

        redisCacheService.setNotificationMode(testChatId, "immediate");

        LinkUpdate linkUpdate = new LinkUpdate(1L, testUrl, testDescription, List.of(testChatId));

        // Act
        kafkaTemplate.send(linkUpdatesTopic, linkUpdate).get();

        // Assert
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(telegramClient).sendMessage(chatIdCaptor.capture(), messageCaptor.capture());

            assertThat(chatIdCaptor.getValue()).isEqualTo(testChatId);
            assertThat(messageCaptor.getValue()).contains(testUrl).contains(testDescription);
        });
    }
}
