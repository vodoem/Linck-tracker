package backend.academy.bot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.service.RedisCacheService;
import backend.academy.model.LinkUpdate;
import com.redis.testcontainers.RedisContainer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(properties = {"spring.kafka.consumer.auto-offset-reset=earliest", "app.message-transport=Kafka"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaConsumerServiceTest {

    @Autowired
    private KafkaTemplate<String, LinkUpdate> kafkaTemplate;

    @Autowired
    private RedisCacheService redisCacheService;

    @MockitoBean
    private TelegramClient telegramClient;

    @Value("${kafka.topic.link-updates}")
    private String linkUpdatesTopic;

    @Container
    private static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);

    @Container
    private static final RedisContainer REDIS_CONTAINER = new RedisContainer("redis:6.2.6").withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Настройка Redis
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));

        // Настройка Kafka
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    @BeforeEach
    void clearRedisNotificationData() {
        // Очищаем все данные связанные с уведомлениями
        redisCacheService.clearAllNotificationData();
        redisCacheService.clearAllRedisKeys();
    }

    @AfterEach
    void clearRedisNotificationDataAfterTest() {
        // Очищаем все данные связанные с уведомлениями
        clearRedisNotificationData();
    }

    @Test
    void shouldProcessValidMessageImmediately() throws Exception {
        // Arrange
        Long chatId = 1L;
        LinkUpdate update = new LinkUpdate(chatId, "https://example.com", "New content available", List.of(chatId));

        redisCacheService.setNotificationMode(chatId, "immediate");

        // Act
        kafkaTemplate.send(linkUpdatesTopic, update).get();

        // Assert
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(telegramClient).sendMessage(ArgumentMatchers.eq(chatId), ArgumentMatchers.contains(update.url()));
        });
    }

    @Test
    void shouldStoreNotificationForDigestMode() throws Exception {
        // Arrange
        Long chatId = 2L;
        LinkUpdate update = new LinkUpdate(chatId, "https://example.org", "Updated documentation", List.of(chatId));

        redisCacheService.setNotificationMode(chatId, "digest");

        // Act
        kafkaTemplate.send(linkUpdatesTopic, update).get();

        // Assert
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            List<String> notifications = redisCacheService.getNotificationsFromBatch(chatId);
            assertThat(notifications)
                    .hasSize(1)
                    .anyMatch(msg -> msg.contains(update.url()) && msg.contains(update.description()));
        });
    }
}
