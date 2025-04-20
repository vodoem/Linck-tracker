package backend.academy.bot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import backend.academy.bot.AbstractIntegrationTest;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.service.RedisCacheService;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class KafkaDlqIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.link-updates}")
    private String linkUpdatesTopic;

    @Value("${kafka.topic.dead-letter-queue}")
    private String dlqTopic;

    @MockitoBean
    private RedisCacheService redisCacheService;

    @MockitoBean
    private TelegramClient telegramClient;

    @Test
    void shouldRedirectInvalidJsonToDlq() throws Exception {
        // Arrange: Невалидный JSON
        String invalidJson = "invalid-json";

        // Act: Отправляем сообщение в основной топик
        kafkaTemplate.send(linkUpdatesTopic, invalidJson).get();

        // Assert: Проверяем, что сообщение попало в DLQ
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> records = getRecordsFromTopic(dlqTopic);
            assertThat(records.count()).isGreaterThan(0);

            // Проверяем содержимое сообщения
            records.forEach(record -> {
                assertThat(record.value()).contains("invalid-json");
            });
        });
    }

    @Test
    void shouldRedirectInvalidDataToDlq() throws Exception {
        // Arrange: Невалидные данные (отсутствует обязательное поле)
        String invalidData =
                """
            {
                "url": "https://example.com",
                "description": "Test update"
            }
        """;

        // Act: Отправляем сообщение в основной топик
        kafkaTemplate.send(linkUpdatesTopic, invalidData).get();

        // Assert: Проверяем, что сообщение попало в DLQ
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> records = getRecordsFromTopic(dlqTopic);
            assertThat(records.count()).isGreaterThan(0);

            // Проверяем содержимое сообщения
            records.forEach(record -> {
                assertThat(record.value()).contains("Test update");
            });
        });
    }

    private ConsumerRecords<String, String> getRecordsFromTopic(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            return consumer.poll(Duration.ofMillis(5000));
        }
    }
}
