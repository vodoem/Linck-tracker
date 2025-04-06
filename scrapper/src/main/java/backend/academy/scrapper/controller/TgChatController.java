package backend.academy.scrapper.controller;

import backend.academy.model.ApiErrorResponse;
import backend.academy.scrapper.exceptionhandler.ResourceNotFoundException;
import backend.academy.scrapper.service.LinkService;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tg-chat")
public class TgChatController {

    private final LinkService linkService;

    public TgChatController(LinkService linkService) {
        this.linkService = linkService;
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

        // Регистрируем чат в репозитории
        linkService.registerChat(id);

        System.out.println("Чат зарегистрирован: " + id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable long id) {

        // Удаляем чат из репозитория
        linkService.deleteChat(id);

        System.out.println("Чат удален: " + id);
        return ResponseEntity.ok().build();
    }
}
