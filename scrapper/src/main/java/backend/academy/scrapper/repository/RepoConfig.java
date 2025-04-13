package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.repos.TgChatRepository;
import backend.academy.scrapper.repository.repos.TrackedLinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RepoConfig {
    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
    public LinkRepository sqlLinkRepository(JdbcTemplate jdbcTemplate) {
        return new SqlLinkRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
    public LinkRepository ormLinkRepository(
            TgChatRepository tgChatRepository, TrackedLinkRepository trackedLinkRepository) {
        return new OrmLinkRepository(tgChatRepository, trackedLinkRepository);
    }
}
