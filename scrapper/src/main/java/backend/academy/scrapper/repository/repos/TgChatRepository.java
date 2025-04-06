package backend.academy.scrapper.repository.repos;

import backend.academy.scrapper.repository.DTO.TgChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TgChatRepository extends JpaRepository<TgChat, Long> {
    Optional<TgChat> findById(Long chatId);
}
