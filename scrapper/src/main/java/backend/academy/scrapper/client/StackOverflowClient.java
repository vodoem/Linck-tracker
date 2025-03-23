package backend.academy.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class StackOverflowClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String accessToken;

    public StackOverflowClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "https://api.stackexchange.com/2.3";
        this.apiKey = System.getenv("SO_TOKEN_KEY");
        this.accessToken = System.getenv("SO_ACCESS_TOKEN");
    }

    // Получение даты последней активности
    public long getLastActivityDate(int questionId, String site) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/questions/" + questionId)
                .queryParam("site", site)
                .queryParam("filter", "!9_bDDxJY5") // Стандартный фильтр
                .queryParam("key", apiKey)
                .queryParam("access_token", accessToken)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode body = response.getBody();
        if (body == null || !body.has("items") || body.get("items").isEmpty()) {
            throw new RuntimeException("Invalid response from StackExchange API: 'items' field is missing or empty.");
        }

        return body.get("items").get(0).get("last_activity_date").asLong();
    }

    // Получение деталей вопроса и последнего ответа
    public JsonNode getQuestionDetails(int questionId, String site) {
        String url = baseUrl + "/questions/" + questionId + "?site=" + site + "&filter=withbody";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        return response.getBody();
    }

    // Получение последнего ответа
    public JsonNode getLastAnswer(int questionId, String site) {
        String url = baseUrl + "/questions/" + questionId + "/answers?site=" + site
                + "&order=desc&sort=creation&filter=withbody";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode body = response.getBody();
        if (body == null || !body.has("items") || body.get("items").isEmpty()) {
            return null;
        }

        return body.get("items").get(0); // Берем последний ответ
    }
}
