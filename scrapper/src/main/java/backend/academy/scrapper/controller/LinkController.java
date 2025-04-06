package backend.academy.scrapper.controller;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.service.LinkService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
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
    private final LinkService linkService;

    @Value("${app.db.batch-size}")
    private int batchSize;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        System.out.println("Получение ссылок для чата: " + chatId);

        // Параметры пагинации
        int offset = 0;
        int limit = batchSize; // Размер пакета из конфигурации
        List<LinkResponse> allLinks = new ArrayList<>();

        while (true) {
            // Получаем пакет ссылок
            ListLinksResponse response = linkService.getLinks(chatId, offset, limit);
            List<LinkResponse> links = response.links();

            // Если больше нет данных, завершаем цикл
            if (links.isEmpty()) {
                break;
            }

            // Добавляем загруженные ссылки в общий список
            allLinks.addAll(links);

            // Переходим к следующему пакету
            offset += limit;
        }

        // Создаем объект ListLinksResponse с данными
        ListLinksResponse listLinksResponse = new ListLinksResponse(allLinks, allLinks.size());

        // Возвращаем ответ
        return ResponseEntity.ok(listLinksResponse);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest request) {
        System.out.println("Добавление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Добавляем ссылку в репозиторий
        linkService.addLink(chatId, request);

        // Возвращаем ответ
        LinkResponse response = new LinkResponse(chatId, request.link(), request.tags(), request.filters());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest request) {
        System.out.println("Удаление ссылки для чата: " + chatId + ", ссылка: " + request.link());

        // Удаляем ссылку из репозитория
        linkService.removeLink(chatId, request);

        // Возвращаем успешный ответ
        return ResponseEntity.ok().build();
    }
}
