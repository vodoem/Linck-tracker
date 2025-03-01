package backend.academy.scrapper.controller;

import backend.academy.scrapper.model.AddLinkRequest;
import backend.academy.scrapper.model.LinkResponse;
import backend.academy.scrapper.model.ListLinksResponse;
import backend.academy.scrapper.model.RemoveLinkRequest;
import backend.academy.scrapper.repository.HttpLinkRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
public class LinkController {
    private final HttpLinkRepository linkRepository;

    public LinkController(HttpLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @GetMapping
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        System.out.println("Получение ссылок для чата: " + chatId);

        // Получаем список ссылок из репозитория
        List<LinkResponse> links = linkRepository.getLinks(chatId);

        // Создаем объект ListLinksResponse с данными
        ListLinksResponse listLinksResponse = new ListLinksResponse(links, links.size());

        // Возвращаем ответ
        return ResponseEntity.ok(listLinksResponse);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest request) {
        System.out.println("Добавление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Добавляем ссылку в репозиторий
        linkRepository.addLink(chatId, request.link(), request.tags(), request.filters());

        // Возвращаем ответ
        LinkResponse response = new LinkResponse(chatId, request.link(), request.tags(), request.filters());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest request) {
        System.out.println("Удаление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Удаляем ссылку из репозитория
        linkRepository.removeLink(chatId, request.link());

        // Возвращаем успешный ответ
        return ResponseEntity.ok().build();
    }
}
