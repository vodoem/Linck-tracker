package backend.academy.scrapper.controller;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.LinkResponse;
import backend.academy.bot.model.ListLinksResponse;
import backend.academy.bot.model.RemoveLinkRequest;
import backend.academy.scrapper.repository.HttpLinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> links = linkRepository.getLinks(chatId);
        List<LinkResponse> responseLinks = links.stream()
            .map(link -> new LinkResponse(chatId, link, List.of(), List.of()))
            .collect(Collectors.toList());

        // Создаем объект ListLinksResponse с данными
        ListLinksResponse listLinksResponse = new ListLinksResponse(responseLinks, responseLinks.size());

        // Возвращаем ответ
        return ResponseEntity.ok(listLinksResponse);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
        @RequestHeader("Tg-Chat-Id") long chatId,
        @RequestBody AddLinkRequest request
    ) {
        System.out.println("Добавление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Добавляем ссылку в репозиторий
        linkRepository.addLink(chatId, request.link());

        // Возвращаем ответ
        LinkResponse response = new LinkResponse(chatId, request.link(), request.tags(), request.filters());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeLink(
        @RequestHeader("Tg-Chat-Id") long chatId,
        @RequestBody RemoveLinkRequest request
    ) {
        System.out.println("Удаление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Удаляем ссылку из репозитория
        linkRepository.removeLink(chatId, request.link());

        // Возвращаем успешный ответ
        return ResponseEntity.ok().build();
    }
}
