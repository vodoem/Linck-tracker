package backend.academy.scrapper.controller;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.RemoveLinkRequest;
import backend.academy.scrapper.exceptionhandler.ResourceNotFoundException;
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
        if (!isValidChatId(id)) {
            throw new IllegalArgumentException("Некорректный ID чата");
        }

        // Регистрируем чат в репозитории
        linkRepository.registerChat(id);

        System.out.println("Чат зарегистрирован: " + id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable long id) {
        if (!chatExists(id)) {
            throw new ResourceNotFoundException("Чат с ID " + id + " не существует");
        }

        // Удаляем чат из репозитория
        linkRepository.getAllChatIds().remove(id);

        System.out.println("Чат удален: " + id);
        return ResponseEntity.ok().build();
    }

    private boolean isValidChatId(long id) {
        // Проверка корректности ID
        return id > 0;
    }

    private boolean chatExists(long id) {
        // Проверка существования чата
        return linkRepository.getAllChatIds().contains(id); // Пример
    }
}
