package backend.academy.scrapper.client;

import backend.academy.model.LinkUpdate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class BotClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BotClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8080";
    }

    // Отправка обновления
    public void sendUpdate(LinkUpdate update) {
        String url = baseUrl + "/updates";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<LinkUpdate> entity = new HttpEntity<>(update, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка отправки обновления: " + e.getResponseBodyAsString(), e);
        }
    }
}
