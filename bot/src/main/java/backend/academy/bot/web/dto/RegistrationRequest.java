package backend.academy.bot.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "Введите имя пользователя")
                @Size(min = 3, max = 50, message = "Имя должно содержать от 3 до 50 символов")
                String username,
        @NotBlank(message = "Введите пароль")
                @Size(min = 6, max = 100, message = "Пароль должен содержать не менее 6 символов")
                String password) {}
