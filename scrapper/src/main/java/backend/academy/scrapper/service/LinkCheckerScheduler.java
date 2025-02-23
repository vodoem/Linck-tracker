package backend.academy.scrapper.service;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.bot.model.LinkUpdate;
import backend.academy.scrapper.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LinkCheckerScheduler {
    private final LinkRepository linkRepository;
    private final BotClient botClient;
    private final Map<String, LinkChecker> linkCheckers; // Карта для хранения стратегий
    private static final Logger logger = LoggerFactory.getLogger(LinkCheckerScheduler.class);

    public LinkCheckerScheduler(LinkRepository linkRepository, BotClient botClient,
                                List<LinkChecker> linkCheckers) {
        this.linkRepository = linkRepository;
        this.botClient = botClient;

        // Инициализация карты стратегий
        this.linkCheckers = linkCheckers.stream()
            .collect(Collectors.toMap(
                checker -> checker.getClass().getSimpleName().replace("LinkChecker", "").toLowerCase(),
                Function.identity()
            ));
    }

    @Scheduled(fixedRate = 30000) // Каждые 30 секунд
    public void checkLinks() {
        List<Long> chatIds = linkRepository.getAllChatIds(); // Получаем все chatId
        for (long chatId : chatIds) {
            List<String> links = linkRepository.getLinks(chatId); // Получаем ссылки для каждого чата
            if (!links.isEmpty()) { // Проверяем только чаты с ссылками
                for (String link : links) {
                    processLink(chatId, link);
                }
            }
        }
    }

    private void processLink(long chatId, String link) {
        String platform = detectPlatform(link); // Определяем платформу по ссылке
        LinkChecker checker = linkCheckers.get(platform);

        if (checker != null && checker.checkForUpdates(link)) {
            String description = checker.getUpdateDescription(link);
            sendUpdate(chatId, link, description);
        }
    }

    private String detectPlatform(String link) {
        if (link.contains("github.com")) {
            return "github";
        } else if (link.contains("stackoverflow.com")) {
            return "stackoverflow";
        }
        // Добавьте другие платформы здесь
        return null;
    }

    private void sendUpdate(long chatId, String link, String description) {
        LinkUpdate update = new LinkUpdate(
            chatId,
            link,
            description,
            List.of(chatId) // Отправляем уведомление только этому чату
        );
        logger.info("Отправка уведомления: " + update);
        botClient.sendUpdate(update);
    }
}

