package backend.academy.bot;

import backend.academy.bot.client.BotClient;
import backend.academy.bot.client.GitHubClient;
import backend.academy.bot.client.StackOverflowClient;
import backend.academy.bot.model.LinkUpdate;
import backend.academy.bot.repository.LinkRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LinkCheckerScheduler {
    private final LinkRepository linkRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;

    // Кэш для хранения последних дат обновлений
    private final Map<String, Instant> lastUpdatedCache = new ConcurrentHashMap<>();
    private final Map<String, Long> linkToChatIdMap = new ConcurrentHashMap<>();

    public LinkCheckerScheduler(LinkRepository linkRepository, GitHubClient gitHubClient,
                                StackOverflowClient stackOverflowClient, BotClient botClient) {
        this.linkRepository = linkRepository;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        this.botClient = botClient;
    }

    @Scheduled(fixedRate = 6000) // Каждую минуту
    public void checkLinks() {
        List<Long> chatIds = linkRepository.getAllChatIds(); // Получаем все chatId

        for (long chatId : chatIds) {
            List<String> links = linkRepository.getLinks(chatId); // Получаем ссылки для каждого чата

            if (!links.isEmpty()) { // Проверяем только чаты с ссылками
                for (String link : links) {
                    linkToChatIdMap.put(link, chatId); // Сохраняем связь ссылка -> chatId

                    if (link.contains("github.com")) {
                        checkGitHubLink(link);
                    } else if (link.contains("stackoverflow.com")) {
                        checkStackOverflowLink(link);
                    }
                }
            }
        }
    }

    private void checkGitHubLink(String link) {
        String[] parts = link.split("/"); // Разбиваем ссылку на части
        String owner = parts[3];
        String repo = parts[4];
        String lastUpdated = gitHubClient.getLastUpdatedDate(owner, repo);
        Instant lastUpdatedInstant = Instant.parse(lastUpdated);

        String cachedKey = "github:" + owner + "/" + repo;
        Instant cachedDate = lastUpdatedCache.getOrDefault(cachedKey, Instant.MIN);

        if (lastUpdatedInstant.isAfter(cachedDate)) {
            lastUpdatedCache.put(cachedKey, lastUpdatedInstant);
            Long chatId = linkToChatIdMap.get(link); // Получаем chatId для ссылки
            if (chatId != null) {
                sendUpdate(chatId, link, "Репозиторий обновлен: " + repo);
            }
        }
    }

    private void checkStackOverflowLink(String link) {
        int questionId = Integer.parseInt(link.split("/")[4]);
        long lastActivityDate = stackOverflowClient.getLastActivityDate(questionId);
        Instant lastActivityInstant = Instant.ofEpochSecond(lastActivityDate);

        String cachedKey = "stackoverflow:" + questionId;
        Instant cachedDate = lastUpdatedCache.getOrDefault(cachedKey, Instant.MIN);

        if (lastActivityInstant.isAfter(cachedDate)) {
            lastUpdatedCache.put(cachedKey, lastActivityInstant);
            Long chatId = linkToChatIdMap.get(link); // Получаем chatId для ссылки
            if (chatId != null) {
                sendUpdate(chatId, link, "Новый ответ на вопрос: " + questionId);
            }
        }
    }

    private void sendUpdate(long chatId, String link, String description) {
        LinkUpdate update = new LinkUpdate(
            1L, // ID ссылки (временно фиксировано)
            link,
            description,
            List.of(chatId) // Отправляем уведомление только этому чату
        );
        botClient.sendUpdate(update);
    }
}

