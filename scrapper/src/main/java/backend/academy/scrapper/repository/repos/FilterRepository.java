package backend.academy.scrapper.repository.repos;

import backend.academy.scrapper.repository.DTO.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Long> {
    // Пока тут пусто
}
