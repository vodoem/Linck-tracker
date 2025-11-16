package backend.academy.bot.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebUserRepository extends JpaRepository<WebUser, Long> {
    boolean existsByUsername(String username);

    Optional<WebUser> findByUsername(String username);
}
