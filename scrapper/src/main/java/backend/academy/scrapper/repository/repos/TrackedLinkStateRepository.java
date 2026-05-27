package backend.academy.scrapper.repository.repos;

import backend.academy.scrapper.repository.DTO.TrackedLinkState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedLinkStateRepository extends JpaRepository<TrackedLinkState, Long> {
    Optional<TrackedLinkState> findByTrackedLinkId(Long trackedLinkId);
}
