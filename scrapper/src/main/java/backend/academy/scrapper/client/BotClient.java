package backend.academy.scrapper.client;

import backend.academy.model.LinkUpdate;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class BotClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Value("${http.retry.retryable-status-codes}")
    private Set<Integer> retryableStatusCodes;

    public BotClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка отправки обновления: " + e.getResponseBodyAsString(), e);
        }
    }

    private boolean shouldRetry(HttpStatusCode status) {
        return retryableStatusCodes.contains(status.value());
    }
}
