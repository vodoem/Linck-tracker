package backend.academy.bot;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
public abstract class AbstractIntegrationTest {

    protected static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:6.2.6")).withExposedPorts(6379);

    protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer("apache/kafka-native:3.8.1")
        .withExposedPorts(9092);

    static {
        REDIS_CONTAINER.start();
        KAFKA_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Настройка Redis
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));

        // Настройка Kafka
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }
}
