package backend.academy.scrapper;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.repository.LiquibaseMigrationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({ScrapperConfig.class})
@EnableScheduling
@EnableRetry
public class ScrapperApplication {
    private static final String DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:5433/scrapper";
    private static final String DEFAULT_DB_USERNAME = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "postgres";
    private static final String DEFAULT_CHANGELOG_PATH = "migrations/master.xml";

    public static void main(String[] args) {
        runMigrations();
        SpringApplication.run(ScrapperApplication.class, args);
    }

    private static void runMigrations() {
        String jdbcUrl = getEnvOrDefault("DB_URL", DEFAULT_JDBC_URL);
        String username = getEnvOrDefault("DB_USERNAME", DEFAULT_DB_USERNAME);
        String password = getEnvOrDefault("DB_PASSWORD", DEFAULT_DB_PASSWORD);
        String changelogPath = getEnvOrDefault("DB_CHANGELOG_PATH", DEFAULT_CHANGELOG_PATH);

        try {
            LiquibaseMigrationRunner.runMigrations(jdbcUrl, username, password, changelogPath);
        } catch (Exception e) {
            System.err.println("Не удалось выполнить миграции: " + e.getMessage());
            throw new RuntimeException("Ошибка при выполнении миграций", e);
        }
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
