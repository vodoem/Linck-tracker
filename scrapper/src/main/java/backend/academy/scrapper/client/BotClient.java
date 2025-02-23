package backend.academy.scrapper.client;

import backend.academy.bot.model.LinkUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Component
public class BotClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BotClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8080"; // URL bot API
    }

    // Отправка обновления
    public void sendUpdate(LinkUpdate update) {
        String url = baseUrl + "/updates";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<LinkUpdate> entity = new HttpEntity<>(update, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send update");
        }
    }
}
