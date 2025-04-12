package backend.academy.scrapper.service;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.BotClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP")
public class HttpCommunicationService implements CommunicationService {

    private final BotClient botClient;

    public HttpCommunicationService(BotClient botClient) {
        this.botClient = botClient;
    }

    @Override
    public void sendUpdate(LinkUpdate update) {
        botClient.sendUpdate(update);
    }
}
