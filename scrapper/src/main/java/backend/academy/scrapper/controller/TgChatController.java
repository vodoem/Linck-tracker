package backend.academy.scrapper.controller;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.RemoveLinkRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tg-chat")
public class TgChatController {

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable long id) {
        // Логика регистрации чата
        System.out.println("Чат зарегистрирован: " + id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable long id) {
        // Логика удаления чата
        System.out.println("Чат удален: " + id);
        return ResponseEntity.ok().build();
    }
}
