package backend.academy.scrapper.repository;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import java.sql.Connection;
import java.sql.DriverManager;

public class LiquibaseMigrationRunner {

    public static void runMigrations(String jdbcUrl, String username, String password, String changelogPath) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                changelogPath,
                new ClassLoaderResourceAccessor(),
                database
            );

            liquibase.update("");
            System.out.println("Миграции успешно выполнены.");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении миграций", e);
        }
    }
}
