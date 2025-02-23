package backend.academy.scrapper.controller;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.RemoveLinkRequest;
import backend.academy.scrapper.repository.HttpLinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tg-chat")
public class TgChatController {
    private final HttpLinkRepository linkRepository;

    public TgChatController(HttpLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable long id) {
        System.out.println("Регистрация чата: " + id);

        // Регистрируем чат в репозитории
        linkRepository.registerChat(id);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable long id) {
        System.out.println("Удаление чата: " + id);

        // Удаляем чат из репозитория
        linkRepository.getAllChatIds().remove(id);

        return ResponseEntity.ok().build();
    }
}
