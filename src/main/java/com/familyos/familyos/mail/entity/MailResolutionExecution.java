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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "mail_resolution_executions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_mail_resolution_executions_conflict_action",
        columnNames = {"conflict_id", "action_key"}
    ),
    indexes = @Index(name = "idx_mail_resolution_executions_conflict", columnList = "conflict_id")
)
public class MailResolutionExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conflict_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mail_resolution_executions_conflict_id"))
    private MailConflict conflict;

    @Column(name = "action_key", nullable = false, length = 100)
    private String actionKey;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "calendar_updated_at", nullable = false)
    private OffsetDateTime calendarUpdatedAt;

    @Column(name = "notification_sent_at")
    private OffsetDateTime notificationSentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MailConflict getConflict() {
        return conflict;
    }

    public void setConflict(MailConflict conflict) {
        this.conflict = conflict;
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public OffsetDateTime getCalendarUpdatedAt() {
        return calendarUpdatedAt;
    }

    public void setCalendarUpdatedAt(OffsetDateTime calendarUpdatedAt) {
        this.calendarUpdatedAt = calendarUpdatedAt;
    }

    public OffsetDateTime getNotificationSentAt() {
        return notificationSentAt;
    }

    public void setNotificationSentAt(OffsetDateTime notificationSentAt) {
        this.notificationSentAt = notificationSentAt;
    }
}
