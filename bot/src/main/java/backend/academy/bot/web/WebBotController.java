package backend.academy.bot.web;

import backend.academy.bot.service.BotService;
import backend.academy.bot.user.WebUserDetails;
import java.security.Principal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ui")
public class WebBotController {
    private final ChatSessionService chatSessionService;
    private final BotService botService;

    public WebBotController(ChatSessionService chatSessionService, BotService botService) {
        this.chatSessionService = chatSessionService;
        this.botService = botService;
    }

    @GetMapping("/chat")
    public String chat(@AuthenticationPrincipal WebUserDetails userDetails, Model model, Principal principal) {
        long chatId = userDetails.getId();
        model.addAttribute("username", principal.getName());
        model.addAttribute("messages", chatSessionService.immutableHistory(chatId));
        return "chat";
    }

    @PostMapping("/chat/send")
    public String sendMessage(
            @AuthenticationPrincipal WebUserDetails userDetails,
            @RequestParam("message") String message) {
        long chatId = userDetails.getId();
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) {
            return "redirect:/ui/chat";
        }
        chatSessionService.appendUserMessage(chatId, trimmed);
        String response = trimmed.startsWith("/")
                ? botService.handleCommand(trimmed, chatId)
                : botService.handleTextMessage(chatId, trimmed);
        chatSessionService.appendBotMessage(chatId, response);
        return "redirect:/ui/chat";
    }
}
