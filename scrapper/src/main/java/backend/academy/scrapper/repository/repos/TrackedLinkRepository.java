package backend.academy.scrapper.repository.repos;

import backend.academy.scrapper.repository.DTO.TrackedLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackedLinkRepository extends JpaRepository<TrackedLink, Long> {
    Optional<TrackedLink> findByChatIdAndUrl(Long chatId, String url);

    @Query("SELECT DISTINCT tl FROM TrackedLink tl " + "LEFT JOIN FETCH tl.tags t "
            + "LEFT JOIN FETCH tl.filters f "
            + "WHERE tl.chat.id = :chatId")
    List<TrackedLink> findByChatId(@Param("chatId") Long chatId, Pageable pageable);

    @Query(
            "SELECT DISTINCT tl FROM TrackedLink tl JOIN FETCH tl.tags t WHERE tl.chat.id = :chatId AND t.name = :tagName")
    List<TrackedLink> findByChatIdAndTagName(@Param("chatId") Long chatId, @Param("tagName") String tagName);

    boolean existsByChatIdAndUrl(long chatId, String url);

    @Query("SELECT f.value FROM TrackedLink tl " + "JOIN tl.filters f "
            + "WHERE tl.chat.id = :chatId AND tl.url = :url")
    List<String> getFiltersForLink(@Param("chatId") long chatId, @Param("url") String url);

    @Query("SELECT DISTINCT tl.chat.id FROM TrackedLink tl WHERE tl.url = :url")
    List<Long> findChatIdsByUrl(@Param("url") String url);
}
