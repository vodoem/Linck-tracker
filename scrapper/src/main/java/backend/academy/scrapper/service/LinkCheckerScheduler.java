package backend.academy.scrapper.service;

import backend.academy.scrapper.client.BotClient;
import backend.academy.model.LinkResponse;
import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LinkCheckerScheduler {
    private final LinkRepository linkRepository;
    private final BotClient botClient;
    private Map<String, LinkChecker> linkCheckers; // Карта для хранения стратегий
    @Value("${app.db.batch-size}")
    private int batchSize;

    public LinkCheckerScheduler(LinkRepository linkRepository, BotClient botClient, List<LinkChecker> linkCheckers) {
        this.linkRepository = linkRepository;
        this.botClient = botClient;

        // Инициализация карты стратегий
        this.linkCheckers = (linkCheckers != null)
                ? linkCheckers.stream()
                        .collect(Collectors.toMap(
                                checker -> checker.getClass()
                                        .getSimpleName()
                                        .replace("LinkChecker", "")
                                        .toLowerCase(),
                                Function.identity()))
                : Collections.emptyMap();
    }

    public void setLinkCheckers(List<LinkChecker> linkCheckers) {
        this.linkCheckers = (linkCheckers != null)
                ? linkCheckers.stream()
                        .collect(Collectors.toMap(
                                checker -> checker.getClass()
                                        .getSimpleName()
                                        .replace("LinkChecker", "")
                                        .toLowerCase(),
                                Function.identity()))
                : Collections.emptyMap();
    }

    @Scheduled(fixedDelayString = "${app.scheduler.delay-ms}") // Каждые 30 секунд
    public void checkLinks() {
        List<Long> chatIds = linkRepository.getAllChatIds(); // Получаем все chatId
        for (long chatId : chatIds) {
            int offset = 0;
            int limit = batchSize;
            while (true) {
                List<LinkResponse> links = linkRepository.getLinks(chatId, offset, limit);
                if (links.isEmpty()) {
                    break; // Если больше нет ссылок, завершаем цикл
                }
                else { // Проверяем только чаты с ссылками
                    for (String link : links.stream().map(LinkResponse::url).toList()) {
                        processLink(chatId, link);
                    }
                }
                offset += limit; // Переходим к следующему пакету
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
                chatId, link, description, List.of(chatId) // Отправляем уведомление только этому чату
                );
        botClient.sendUpdate(update);
    }
}
