package backend.academy.scrapper.repository;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;

public class LiquibaseMigrationRunner {

    public static void runMigrations(String jdbcUrl, String username, String password, String changelogPath) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            // Получаем текущую рабочую директорию
            String currentDir = Path.of("").toAbsolutePath().toString();

            // Проверяем, откуда запускаем
            String projectRoot;
            if (currentDir.endsWith("java-vodoem")) {
                projectRoot = currentDir; // Запуск из корня проекта
            } else {
                Path parentDir = Path.of(currentDir).getParent();
                if (parentDir == null) {
                    throw new RuntimeException("Невозможно определить родительскую директорию для: " + currentDir);
                }
                projectRoot = parentDir.toString(); // Запуск из подпроекта (scrapper)
            }

            Liquibase liquibase = new Liquibase(changelogPath, new FileSystemResourceAccessor(projectRoot), database);

            liquibase.update("");
            System.out.println("Миграции успешно выполнены.");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении миграций", e);
        }
    }
}
