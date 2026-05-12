package backend.academy.scrapper.repository;

import java.nio.file.Files;
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
            Path projectRoot = findProjectRoot(changelogPath);
            Liquibase liquibase =
                    new Liquibase(changelogPath, new FileSystemResourceAccessor(projectRoot.toString()), database);

            liquibase.update("");
            System.out.println("Миграции успешно выполнены.");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении миграций", e);
        }
    }

    private static Path findProjectRoot(String changelogPath) {
        Path currentDir = Path.of("").toAbsolutePath();
        Path dir = currentDir;
        while (dir != null) {
            if (Files.exists(dir.resolve(changelogPath))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException(
                "Не найден Liquibase changelog '" + changelogPath + "' относительно " + currentDir);
    }
}
