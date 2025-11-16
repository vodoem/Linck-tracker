package backend.academy.bot.web;

import backend.academy.bot.service.BotService;
import backend.academy.bot.user.WebUserDetails;
import backend.academy.model.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@RequestMapping("/ui")
public class WebBotController {
    private final ChatSessionService chatSessionService;
    private final BotService botService;
    private final ObjectMapper objectMapper;

    public WebBotController(ChatSessionService chatSessionService, BotService botService, ObjectMapper objectMapper) {
        this.chatSessionService = chatSessionService;
        this.botService = botService;
        this.objectMapper = objectMapper;
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
        try {
            String response = trimmed.startsWith("/")
                    ? botService.handleCommand(trimmed, chatId)
                    : botService.handleTextMessage(chatId, trimmed);
            chatSessionService.appendBotMessage(chatId, response);
        } catch (HttpClientErrorException ex) {
            chatSessionService.appendBotMessage(chatId, mapClientError(ex));
        } catch (Exception ex) {
            chatSessionService.appendBotMessage(chatId, "Произошла ошибка: " + ex.getMessage());
        }
        return "redirect:/ui/chat";
    }

    private String mapClientError(HttpClientErrorException ex) {
        try {
            ApiErrorResponse response = objectMapper.readValue(ex.getResponseBodyAsString(), ApiErrorResponse.class);
            return response.description() != null ? response.description() : "Клиентская ошибка";
        } catch (Exception ignored) {
            return "Клиентская ошибка: " + ex.getStatusCode().value();
        }
    }
}
