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
    public static void main(String[] args) {

        // Настройки для миграций
        String jdbcUrl = "jdbc:postgresql://localhost:5433/scrapper";
        String username = "postgres";
        String password = System.getenv("DB_PASSWORD");
        String changelogPath = "/migrations/master.xml";

        // Запуск миграций
        try {
            LiquibaseMigrationRunner.runMigrations(jdbcUrl, username, password, changelogPath);
        } catch (Exception e) {
            System.err.println("Не удалось выполнить миграции: " + e.getMessage());
            throw new RuntimeException("Ошибка при выполнении миграций", e);
        }

        SpringApplication.run(ScrapperApplication.class, args);
    }
}
