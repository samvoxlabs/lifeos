package com.familyos.familyos.mail.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "mail_conflicts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_mail_conflicts_event_target",
        columnNames = {"extracted_event_id", "conflicting_event_id", "conflicting_event_source"}
    ),
    indexes = @Index(name = "idx_mail_conflicts_status", columnList = "status")
)
public class MailConflict {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "extracted_event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mail_conflicts_extracted_event_id"))
    private MailExtractedEvent extractedEvent;

    @Column(name = "conflicting_event_id", nullable = false, length = 255)
    private String conflictingEventId;

    @Column(name = "conflicting_event_source", nullable = false, length = 50)
    private String conflictingEventSource;

    @Column(name = "overlap_start", nullable = false)
    private OffsetDateTime overlapStart;

    @Column(name = "overlap_end", nullable = false)
    private OffsetDateTime overlapEnd;

    @Column(nullable = false, length = 50)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "suggested_resolutions_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> suggestedResolutions;

    @Column(name = "applied_resolution_key", length = 100)
    private String appliedResolutionKey;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MailExtractedEvent getExtractedEvent() {
        return extractedEvent;
    }

    public void setExtractedEvent(MailExtractedEvent extractedEvent) {
        this.extractedEvent = extractedEvent;
    }

    public String getConflictingEventId() {
        return conflictingEventId;
    }

    public void setConflictingEventId(String conflictingEventId) {
        this.conflictingEventId = conflictingEventId;
    }

    public String getConflictingEventSource() {
        return conflictingEventSource;
    }

    public void setConflictingEventSource(String conflictingEventSource) {
        this.conflictingEventSource = conflictingEventSource;
    }

    public OffsetDateTime getOverlapStart() {
        return overlapStart;
    }

    public void setOverlapStart(OffsetDateTime overlapStart) {
        this.overlapStart = overlapStart;
    }

    public OffsetDateTime getOverlapEnd() {
        return overlapEnd;
    }

    public void setOverlapEnd(OffsetDateTime overlapEnd) {
        this.overlapEnd = overlapEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getSuggestedResolutions() {
        return suggestedResolutions;
    }

    public void setSuggestedResolutions(Map<String, Object> suggestedResolutions) {
        this.suggestedResolutions = suggestedResolutions;
    }

    public String getAppliedResolutionKey() {
        return appliedResolutionKey;
    }

    public void setAppliedResolutionKey(String appliedResolutionKey) {
        this.appliedResolutionKey = appliedResolutionKey;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
