package backend.academy.scrapper.controller;

import backend.academy.model.AddTagsRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.RemoveTagRequest;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final LinkRepository linkRepository;

    public TagController(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @PostMapping
    public ResponseEntity<Void> addTags(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddTagsRequest request) {
        linkRepository.addTags(chatId, request.url(), request.tags());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeTag(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveTagRequest request) {
        linkRepository.removeTag(chatId, request.url(), request.tagName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> getTagsForLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestParam String url) {
        List<String> tags = linkRepository.getTagsForLink(chatId, url);
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<LinkResponse>> getLinksByTag(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestParam String tagName) {
        List<LinkResponse> links = linkRepository.getLinksByTag(chatId, tagName);
        return ResponseEntity.ok(links); // Возвращаем массив
    }
}
