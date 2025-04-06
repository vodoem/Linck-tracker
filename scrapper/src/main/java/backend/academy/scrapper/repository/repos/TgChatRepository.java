package backend.academy.scrapper.repository.repos;

import backend.academy.scrapper.repository.DTO.TgChat;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TgChatRepository extends JpaRepository<TgChat, Long> {
    @NotNull
    Optional<TgChat> findById(@NotNull Long chatId);
}
