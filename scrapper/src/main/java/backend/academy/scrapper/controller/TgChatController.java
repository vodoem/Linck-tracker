package backend.academy.scrapper.controller;

import backend.academy.model.ApiErrorResponse;
import backend.academy.scrapper.exceptionhandler.ResourceNotFoundException;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tg-chat")
public class TgChatController {
    private final LinkRepository linkRepository;

    public TgChatController(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @ExceptionHandler({IllegalArgumentException.class, ResourceNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleException(RuntimeException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.toString(),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.asList(Arrays.toString(ex.getStackTrace())));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
        linkRepository.deleteChat(id);

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
