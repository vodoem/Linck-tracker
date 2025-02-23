package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GitHubLinkChecker implements LinkChecker {
    private final GitHubClient gitHubClient;
    private final Map<String, Instant> lastUpdatedCache = new ConcurrentHashMap<>();

    public GitHubLinkChecker(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public boolean checkForUpdates(String link) {
        String[] parts = link.split("/");
        String owner = parts[3];
        String repo = parts[4];
        String lastUpdated = gitHubClient.getLastUpdatedDate(owner, repo);
        Instant lastUpdatedInstant = Instant.parse(lastUpdated);

        String cachedKey = "github:" + owner + "/" + repo;
        Instant cachedDate = lastUpdatedCache.getOrDefault(cachedKey, Instant.MIN);

        if (lastUpdatedInstant.isAfter(cachedDate)) {
            lastUpdatedCache.put(cachedKey, lastUpdatedInstant);
            return true;
        }
        return false;
    }

    @Override
    public String getUpdateDescription(String link) {
        String[] parts = link.split("/");
        String repo = parts[4];
        return "Репозиторий обновлен: " + repo;
    }
}
