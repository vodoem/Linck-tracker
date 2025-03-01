package backend.academy.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitHubClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public GitHubClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "https://api.github.com";
    }

    // Получение даты последнего обновления репозитория
    public String getLastUpdatedDate(String owner, String repo) {
        String url = baseUrl + "/repos/" + owner + "/" + repo;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + System.getenv("GITHUB_TOKEN"));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        return Objects.requireNonNull(response.getBody()).get("updated_at").asText();
    }
}
