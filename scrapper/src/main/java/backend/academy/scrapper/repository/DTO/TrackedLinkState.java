package backend.academy.scrapper.repository.DTO;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tracked_link_state")
public class TrackedLinkState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracked_link_state_id")
    private Long trackedLinkStateId;

    @OneToOne
    @JoinColumn(name = "tracked_link_id", nullable = false)
    private TrackedLink trackedLink;

    @Column(name = "last_event_id")
    private String lastEventId;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "state_hash")
    private String stateHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "state_payload", columnDefinition = "jsonb")
    private JsonNode statePayload;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getTrackedLinkStateId() {
        return trackedLinkStateId;
    }

    public void setTrackedLinkStateId(Long trackedLinkStateId) {
        this.trackedLinkStateId = trackedLinkStateId;
    }

    public TrackedLink getTrackedLink() {
        return trackedLink;
    }

    public void setTrackedLink(TrackedLink trackedLink) {
        this.trackedLink = trackedLink;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(String lastEventId) {
        this.lastEventId = lastEventId;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getStateHash() {
        return stateHash;
    }

    public void setStateHash(String stateHash) {
        this.stateHash = stateHash;
    }

    public JsonNode getStatePayload() {
        return statePayload;
    }

    public void setStatePayload(JsonNode statePayload) {
        this.statePayload = statePayload;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
