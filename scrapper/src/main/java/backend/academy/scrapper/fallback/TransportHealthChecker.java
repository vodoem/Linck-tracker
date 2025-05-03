package backend.academy.scrapper.fallback;

import java.util.concurrent.TimeUnit;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TransportHealthChecker {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransportHealthChecker(RestTemplate restTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean isHttpAvailable() {
        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity("http://localhost:8080/actuator/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isKafkaAvailable() {
        try {
            // Отправляем пустое сообщение в тестовый топик
            kafkaTemplate.send("health-check-topic", "").get(2, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
