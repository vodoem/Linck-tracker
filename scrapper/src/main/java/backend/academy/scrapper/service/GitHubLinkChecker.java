package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

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

        // Получаем данные о последнем PR/Issue
        JsonNode issue = gitHubClient.getLatestIssueOrPR(owner, repo);
        if (issue == null || issue.isNull()) {
            return false; // Нет открытых PR/Issue
        }

        int issueNumber = issue.get("number").asInt();
        String cachedKey = "github:" + owner + "/" + repo + "/" + issueNumber;

        Instant lastUpdatedInstant = Instant.parse(issue.get("updated_at").asText());
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
        String owner = parts[3];
        String repo = parts[4];

        // Получаем данные о последнем PR/Issue
        JsonNode issue = gitHubClient.getLatestIssueOrPR(owner, repo);
        if (issue == null || issue.isNull()) {
            return "Нет новых обновлений PR/Issue.";
        }

        String title = issue.get("title").asText();
        String userName = issue.get("user").get("login").asText();
        String createdAt = issue.get("created_at").asText();
        String bodyPreview = issue.has("body") ? truncatePreview(issue.get("body").asText(), 200) : "Нет описания";

        return String.format(
            "Новый PR/Issue: %s\n" +
                "Автор: %s\n" +
                "Дата создания: %s\n" +
                "Превью описания: %s",
            title, userName, createdAt, bodyPreview
        );
    }

    private String truncatePreview(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }
}
