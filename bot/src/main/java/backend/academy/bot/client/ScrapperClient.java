package backend.academy.bot.client;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.LinkResponse;
import backend.academy.bot.model.ListLinksResponse;
import backend.academy.bot.model.RemoveLinkRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Component
public class ScrapperClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ScrapperClient() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8081"; // Убедитесь, что порт правильный
    }

    // 1. Регистрация чата
    public void registerChat(long chatId) {
        String url = baseUrl + "/tg-chat/" + chatId;
        System.out.println("Выполняется запрос к Scrapper API: " + url);

        try {
            restTemplate.postForEntity(url, null, Void.class);
            System.out.println("Чат успешно зарегистрирован: " + chatId);
        } catch (Exception e) {
            System.err.println("Ошибка при регистрации чата: " + e.getMessage());
        }
    }

    // 2. Удаление чата
    public void deleteChat(long chatId) {
        String url = baseUrl + "/tg-chat/" + chatId;
        restTemplate.delete(url);
    }

    public ListLinksResponse getLinks(long chatId) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Используем ParameterizedTypeReference для корректной десериализации ListLinksResponse
        ResponseEntity<ListLinksResponse> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ListLinksResponse>() {}
        );

        return response.getBody();
    }

    // 4. Добавление новой ссылки
    public LinkResponse addLink(long chatId, String link, List<String> tags, List<String> filters) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        AddLinkRequest request = new AddLinkRequest(link, tags, filters);
        HttpEntity<AddLinkRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<LinkResponse> response = restTemplate.postForEntity(url, entity, LinkResponse.class);
        return response.getBody();
    }

    // 5. Удаление ссылки
    public LinkResponse removeLink(long chatId, String link) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        RemoveLinkRequest request = new RemoveLinkRequest(link);
        HttpEntity<RemoveLinkRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        return null;
    }
}
