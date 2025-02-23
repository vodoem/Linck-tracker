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

@Component
public class LinkCheckerScheduler {
    private final LinkRepository linkRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;
    private static final Logger logger = LoggerFactory.getLogger(LinkCheckerScheduler.class);

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

    @Scheduled(fixedRate = 500) // Каждую минуту
    public void checkLinks() {
        List<Long> chatIds = linkRepository.getAllChatIds(); // Получаем все chatId
        logger.info("Проверка ссылок для чатов: " + chatIds);
        for (long chatId : chatIds) {
            logger.info("Проверка ссылок для чата: " + chatId);
            List<String> links = linkRepository.getLinks(chatId); // Получаем ссылки для каждого чата

            if (!links.isEmpty()) { // Проверяем только чаты с ссылками
                for (String link : links) {
                    logger.info("Проверка ссылки: " + link);
                    linkToChatIdMap.put(link, chatId); // Сохраняем связь ссылка -> chatId

                    if (link.contains("github.com")) {
                        logger.info("Проверка GitHub ссылки: " + link);
                        checkGitHubLink(link);
                    } else if (link.contains("stackoverflow.com")) {
                        logger.info("Проверка StackOverflow ссылки: " + link);
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
        logger.info("Проверка GitHub репозитория: " + owner + "/" + repo);
        String lastUpdated = gitHubClient.getLastUpdatedDate(owner, repo);
        Instant lastUpdatedInstant = Instant.parse(lastUpdated);

        String cachedKey = "github:" + owner + "/" + repo;
        Instant cachedDate = lastUpdatedCache.getOrDefault(cachedKey, Instant.MIN);
        logger.info("Последнее обновление: " + lastUpdatedInstant);
        logger.info("Последнее обновление из кэша: " + cachedDate);
        if (lastUpdatedInstant.isAfter(cachedDate)) {

            lastUpdatedCache.put(cachedKey, lastUpdatedInstant);
            Long chatId = linkToChatIdMap.get(link); // Получаем chatId для ссылки
            if (chatId != null) {
                sendUpdate(chatId, link, "Репозиторий обновлен: " + repo);
            }
        }
    }

    private void checkStackOverflowLink(String link) {
        // Извлекаем ID вопроса и домен из ссылки
        String[] parts = link.split("/");
        int questionId = Integer.parseInt(parts[4]);
        String site = parts[2]; // Например, "ru.stackoverflow.com"

        // Получаем дату последней активности с учетом домена
        long lastActivityDate = stackOverflowClient.getLastActivityDate(questionId, site);
        Instant lastActivityInstant = Instant.ofEpochSecond(lastActivityDate);

        // Формируем ключ для кэша
        String cachedKey = site + ":" + questionId;
        Instant cachedDate = lastUpdatedCache.getOrDefault(cachedKey, Instant.MIN);

        // Проверяем, была ли активность после последнего обновления
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
            chatId,
            link,
            description,
            List.of(chatId) // Отправляем уведомление только этому чату
        );
        logger.info("Отправка уведомления: " + update);
        botClient.sendUpdate(update);
    }
}

