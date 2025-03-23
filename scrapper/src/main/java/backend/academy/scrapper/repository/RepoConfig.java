package backend.academy.scrapper.repository;

import jakarta.persistence.EntityManager;
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
    public LinkRepository ormLinkRepository(EntityManager em) {
        return new OrmLinkRepository(em);
    }
}
