package backend.academy.bot;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Component
public class BotStateMachine {
    private final Map<Long, String> states = new HashMap<>();

    public void setState(long chatId, String state) {
        states.put(chatId, state);
    }

    public String getState(long chatId) {
        return states.getOrDefault(chatId, "");
    }

    public void clearState(long chatId) {
        states.remove(chatId);
    }
}
