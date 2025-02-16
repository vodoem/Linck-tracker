package backend.academy.bot.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Component
public class StackOverflowClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StackOverflowClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "https://api.stackexchange.com/2.3";
    }

    // Получение даты последнего обновления вопроса
    public long getLastActivityDate(int questionId) {
        String url = baseUrl + "/questions/" + questionId + "?site=stackoverflow&filter=!9_bDDxJY5";
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            JsonNode.class
        );

        return response.getBody().get("items").get(0).get("last_activity_date").asLong();
    }
}
