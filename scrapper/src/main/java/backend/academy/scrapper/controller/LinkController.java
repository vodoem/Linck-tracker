package backend.academy.scrapper.controller;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.LinkResponse;
import backend.academy.bot.model.ListLinksResponse;
import backend.academy.bot.model.RemoveLinkRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/links")
public class LinkController {

    @GetMapping
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        // Логика получения ссылок для чата
        System.out.println("Получение ссылок для чата: " + chatId);

        // Пример данных
        List<LinkResponse> links = List.of(
            new LinkResponse(1L, "https://example.com", List.of("tag1"), List.of("filter1"))
        );
        return ResponseEntity.ok(new ListLinksResponse(links, links.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
        @RequestHeader("Tg-Chat-Id") long chatId,
        @RequestBody AddLinkRequest request
    ) {
        // Логика добавления ссылки
        System.out.println("Добавление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Пример данных
        LinkResponse response = new LinkResponse(1L, request.link(), request.tags(), request.filters());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
        @RequestHeader("Tg-Chat-Id") long chatId,
        @RequestBody    RemoveLinkRequest request
    ) {
        // Логика удаления ссылки
        System.out.println("Удаление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Пример данных
        LinkResponse response = new LinkResponse(1L, request.link(), List.of(), List.of());
        return ResponseEntity.ok(response);
    }
}
