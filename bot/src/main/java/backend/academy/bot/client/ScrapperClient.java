package backend.academy.bot.client;

import backend.academy.bot.annotations.HttpRetryable;
import backend.academy.model.AddLinkRequest;
import backend.academy.model.AddTagsRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.model.RemoveTagRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ScrapperClient {

    private static final Logger logger = LoggerFactory.getLogger(ScrapperClient.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Value("${http.retry.retryable-status-codes}")
    private Set<Integer> retryableStatusCodes;

    public ScrapperClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.baseUrl = "http://localhost:8081";
    }

    @HttpRetryable
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "registerChatFallback")
    public void registerChat(long chatId) {
        String url = baseUrl + "/tg-chat/" + chatId;
        try {
            restTemplate.postForEntity(url, null, Void.class);
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
        } catch (HttpClientErrorException e) {
            // 4xx ошибки (ни Retry, ни Circuit Breaker)
            throw e;
        }
    }

    @HttpRetryable
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "deleteChatFallback")
    public void deleteChat(long chatId) {
        String url = baseUrl + "/tg-chat/" + chatId;
        try {
            restTemplate.delete(url);
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка удаления чата: " + e.getResponseBodyAsString(), e);
        }
    }

    @HttpRetryable
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "getLinksFallback")
    public ListLinksResponse getLinks(long chatId) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ListLinksResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<ListLinksResponse>() {});
            return response.getBody();
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка получения ссылок: " + e.getResponseBodyAsString(), e);
        }
    }

    @HttpRetryable
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "addLinkFallback")
    public void addLink(long chatId, String link, List<String> tags, List<String> filters) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        AddLinkRequest request = new AddLinkRequest(link, tags, filters);
        HttpEntity<AddLinkRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.postForEntity(url, entity, LinkResponse.class);
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка добавления ссылки: " + e.getResponseBodyAsString(), e);
        }
    }

    @HttpRetryable
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "removeLinkFallback")
    public void removeLink(long chatId, String link) {
        String url = baseUrl + "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        RemoveLinkRequest request = new RemoveLinkRequest(link);
        HttpEntity<RemoveLinkRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка удаления ссылки: " + e.getResponseBodyAsString(), e);
        }
    }

    // 1. Добавление тегов к ссылке
    public void addTags(long chatId, String url, List<String> tags) {
        String urlPath = baseUrl + "/tags";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        AddTagsRequest request = new AddTagsRequest(url, tags);
        HttpEntity<AddTagsRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.postForEntity(urlPath, entity, Void.class);
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка при добавлении тегов: " + e.getResponseBodyAsString(), e);
        }
    }

    // 2. Удаление тега из ссылки
    public void removeTag(long chatId, String url, String tagName) {
        String urlPath = baseUrl + "/tags";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        RemoveTagRequest request = new RemoveTagRequest(url, tagName);
        HttpEntity<RemoveTagRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(urlPath, HttpMethod.DELETE, entity, Void.class);
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка при удалении тега: " + e.getResponseBodyAsString(), e);
        }
    }

    public List<String> getTagsForLink(long chatId, String url) {
        String urlPath = baseUrl + "/tags/list?url=" + url;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<String>> response =
                    restTemplate.exchange(urlPath, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            return response.getBody();
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка при получении тегов: " + e.getResponseBodyAsString(), e);
        }
    }

    public List<LinkResponse> getLinksByTag(long chatId, String tagName) {
        String urlPath = baseUrl + "/tags/filter?tagName=" + tagName;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<LinkResponse>> response =
                    restTemplate.exchange(urlPath, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            return response.getBody(); // Возвращаем список ссылок напрямую
        } catch (HttpServerErrorException e) {
            if (shouldRetry(e.getStatusCode())) {
                throw e; // Будет повтор
            }
            throw new RuntimeException("Ошибка при фильтрации ссылок по тегу: " + e.getResponseBodyAsString(), e);
        }
    }

    private boolean shouldRetry(HttpStatusCode status) {
        return retryableStatusCodes.contains(status.value());
    }

    public void registerChatFallback(long chatId, Throwable t) {
        if (t instanceof HttpClientErrorException e) {
            throw e;
        } else {
            logger.error("Circuit Breaker: Регистрация чата недоступна. Chat ID: {}", chatId);
            throw new RuntimeException("Сервис временно недоступен.");
        }
    }

    public void deleteChatFallback(long chatId, Throwable t) {
        if (t instanceof HttpClientErrorException e) {
            throw e;
        } else {
            logger.error("Circuit Breaker: Удаление чата недоступно. Chat ID: {}", chatId);
            throw new RuntimeException("Сервис временно недоступен.");
        }
    }

    public void addLinkFallback(long chatId, Throwable t) {
        if (t instanceof HttpClientErrorException e) {
            throw e;
        } else {
            logger.error("Circuit Breaker: Добавление ссылки недоступно. Chat ID: {}", chatId);
            throw new RuntimeException("Сервис временно недоступен.");
        }
    }

    public void removeLinkFallback(long chatId, Throwable t) {
        if (t instanceof HttpClientErrorException e) {
            throw e;
        } else {
            logger.error("Circuit Breaker: Удаление ссылки недоступно. Chat ID: {}", chatId);
            throw new RuntimeException("Сервис временно недоступен.");
        }
    }

    public ListLinksResponse getLinksFallback(long chatId, Throwable t) {
        if (t instanceof HttpClientErrorException e) {
            throw e;
        } else {
            logger.error("Circuit Breaker: Получение ссылок недоступно. Chat ID: {}", chatId);
            return new ListLinksResponse(Collections.emptyList(), 0);
        }
    }
}
