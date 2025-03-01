package backend.academy.bot.client;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.LinkResponse;
import backend.academy.bot.model.ListLinksResponse;
import backend.academy.bot.model.RemoveLinkRequest;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ScrapperClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ScrapperClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8081";
    }

    public void registerChat(long chatId) {
        String url = baseUrl + "/tg-chat/" + chatId;
        try {
            restTemplate.postForEntity(url, null, Void.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка регистрации чата: " + e.getResponseBodyAsString(), e);
        }
    }

    public void deleteChat(long chatId) {
        String url = baseUrl + "/tg-chat/" + chatId;
        try {
            restTemplate.delete(url);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка удаления чата: " + e.getResponseBodyAsString(), e);
        }
    }

    public ListLinksResponse getLinks(long chatId) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ListLinksResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<ListLinksResponse>() {});
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка получения ссылок: " + e.getResponseBodyAsString(), e);
        }
    }

    public void addLink(long chatId, String link, List<String> tags, List<String> filters) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        AddLinkRequest request = new AddLinkRequest(link, tags, filters);
        HttpEntity<AddLinkRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.postForEntity(url, entity, LinkResponse.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка добавления ссылки: " + e.getResponseBodyAsString(), e);
        }
    }

    public void removeLink(long chatId, String link) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        RemoveLinkRequest request = new RemoveLinkRequest(link);
        HttpEntity<RemoveLinkRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка удаления ссылки: " + e.getResponseBodyAsString(), e);
        }
    }
}
