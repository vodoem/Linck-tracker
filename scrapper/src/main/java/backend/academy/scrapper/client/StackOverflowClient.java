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

    public StackOverflowClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "https://api.stackexchange.com/2.3";
    }

    public long getLastActivityDate(int questionId, String site) {
        // Формируем URL с параметрами запроса
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/questions/" + questionId)
                .queryParam("site", site)
                .queryParam("filter", "!9_bDDxJY5") // Стандартный фильтр
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Выполняем GET-запрос
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Проверяем ответ API
        JsonNode body = response.getBody();
        if (body == null || !body.has("items") || body.get("items").isEmpty()) {
            throw new RuntimeException("Invalid response from StackExchange API: 'items' field is missing or empty.");
        }

        // Возвращаем дату последней активности
        return body.get("items").get(0).get("last_activity_date").asLong();
    }
}
