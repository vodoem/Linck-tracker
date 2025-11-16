package backend.academy.scrapper.service;

import backend.academy.model.LinkResponse;
import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LinkCheckerScheduler {
    private final LinkRepository linkRepository;
    private final CommunicationService communicationService;
    private Map<String, LinkChecker> linkCheckers; // Карта для хранения стратегий
    private int batchSize;
    private int numThreads;

    @Value("${app.db.batch-size}")
    void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Value("${app.scheduler.num-threads}")
    void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public LinkCheckerScheduler(
            LinkRepository linkRepository, CommunicationService communicationService, List<LinkChecker> linkCheckers) {
        this.linkRepository = linkRepository;
        this.communicationService = communicationService;

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

    @Scheduled(fixedDelayString = "${app.scheduler.delay-ms}") // Каждые N миллисекунд
    public void checkLinks() {
        List<Long> chatIds = linkRepository.getAllChatIds(); // Получаем все chatId
        ExecutorService executor = Executors.newFixedThreadPool(numThreads); // Пул потоков

        for (long chatId : chatIds) {
            int offset = 0;
            while (true) {
                // Загружаем батч ссылок
                List<LinkResponse> links = linkRepository.getLinks(chatId, offset, batchSize);
                if (links.isEmpty()) {
                    break; // Если больше нет ссылок, завершаем цикл
                }

                // Разделяем батч на подзадачи
                List<List<LinkResponse>> batches = splitIntoSubBatches(links, numThreads);

                // Обрабатываем каждую подзадачу в отдельном потоке
                for (List<LinkResponse> subBatch : batches) {
                    executor.submit(() -> processSubBatch(chatId, subBatch));
                }

                offset += batchSize; // Переходим к следующему батчу
            }
        }

        executor.shutdown(); // Останавливаем пул потоков после завершения
    }

    private List<List<LinkResponse>> splitIntoSubBatches(List<LinkResponse> links, int numThreads) {
        // Если список ссылок пустой, возвращаем пустой список
        if (links.isEmpty()) {
            return Collections.emptyList();
        }

        // Если размер списка меньше или равен числу потоков, каждый поток обрабатывает одну ссылку
        if (links.size() <= numThreads) {
            return links.stream()
                    .map(Collections::singletonList) // Каждая ссылка в отдельном списке
                    .toList();
        }

        // Иначе делим список на подбатчи
        int subBatchSize = (int) Math.ceil((double) links.size() / numThreads);
        return IntStream.range(0, numThreads)
                .mapToObj(i -> links.subList(i * subBatchSize, Math.min((i + 1) * subBatchSize, links.size())))
                .filter(subList -> !subList.isEmpty()) // Исключаем пустые подсписки
                .toList();
    }

    private void processSubBatch(long chatId, List<LinkResponse> subBatch) {
        for (LinkResponse linkResponse : subBatch) {
            String link = linkResponse.url();
            String platform = detectPlatform(link); // Определяем платформу по ссылке
            LinkChecker checker = linkCheckers.get(platform);

            if (checker != null && checker.checkForUpdates(link)) {
                String description = checker.getUpdateDescription(link);

                // Извлекаем автора из описания
                String author = extractAuthor(description);
                if (author == null) {
                    continue; // Пропускаем, если автор не найден
                }

                // Получаем фильтры для ссылки
                List<String> filters = linkRepository.getFiltersForLink(chatId, link);

                // Проверяем фильтры
                boolean isFiltered = filters.stream()
                        .filter(filter -> filter.startsWith("user:"))
                        .anyMatch(filter -> filter.equals("user:" + author));

                if (isFiltered) {
                    System.out.println("Уведомление отфильтровано для автора: " + author);
                    continue; // Пропускаем уведомление
                }

                // Отправляем уведомление
                sendUpdate(chatId, link, description);
            }
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
        List<Long> targetChatIds = linkRepository.getChatIdsByUrl(link);
        List<Long> recipients = (targetChatIds == null || targetChatIds.isEmpty())
                ? List.of(chatId)
                : targetChatIds;
        LinkUpdate update = new LinkUpdate(chatId, link, description, recipients);
        communicationService.sendUpdate(update);
    }

    private String extractAuthor(String description) {
        Pattern pattern = Pattern.compile("Автор:\\s*(\\S+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1); // Возвращает имя автора
        }
        return null; // Если автор не найден
    }
}
