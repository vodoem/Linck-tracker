package backend.academy.scrapper.repository.repos;

import backend.academy.scrapper.repository.DTO.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    void deleteByLinkIdAndName(Long linkId, String name);

    List<String> findNamesByLinkId(Long linkId);
}
