package backend.academy.bot.web;

import backend.academy.bot.user.WebUserService;
import backend.academy.bot.web.dto.RegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
public class AuthController {
    private final WebUserService webUserService;

    public AuthController(WebUserService webUserService) {
        this.webUserService = webUserService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest("", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationRequest") RegistrationRequest request, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            webUserService.registerUser(request.username(), request.password());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "register";
        }
        model.addAttribute("success", "Регистрация успешна. Теперь вы можете войти.");
        return "login";
    }
}
