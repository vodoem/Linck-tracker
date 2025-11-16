package backend.academy.bot.controller;

import backend.academy.bot.service.NotificationRouter;
import backend.academy.model.LinkUpdate;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class UpdateController {

    private final NotificationRouter notificationRouter;

    public UpdateController(NotificationRouter notificationRouter) {
        this.notificationRouter = notificationRouter;
    }

    @PostMapping
    @RateLimiter(name = "updateControllerRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Void> handleUpdate(@RequestBody @Valid LinkUpdate linkUpdate) {
        System.out.println("Получено обновление: " + linkUpdate);
        notificationRouter.dispatchUpdate(linkUpdate);
        return ResponseEntity.ok().build();
    }

    // Fallback метод
    public ResponseEntity<String> rateLimitFallback(Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Слишком много запросов. Пожалуйста, попробуйте позже.");
    }
}
