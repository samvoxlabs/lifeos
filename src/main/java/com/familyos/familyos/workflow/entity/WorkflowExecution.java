package com.familyos.familyos.workflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "workflow_executions",
    indexes = {
        @Index(name = "idx_workflow_executions_user_status", columnList = "triggered_by_user_id,status"),
        @Index(name = "idx_workflow_executions_started_at", columnList = "started_at")
    }
)
public class WorkflowExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "triggered_by_user_id", nullable = false, length = 255)
    private String triggeredByUserId;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(name = "emails_processed", nullable = false)
    private int emailsProcessed;

    @Column(name = "events_extracted", nullable = false)
    private int eventsExtracted;

    @Column(name = "conflicts_detected", nullable = false)
    private int conflictsDetected;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WorkflowExecution() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTriggeredByUserId() { return triggeredByUserId; }
    public void setTriggeredByUserId(String triggeredByUserId) { this.triggeredByUserId = triggeredByUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public int getEmailsProcessed() { return emailsProcessed; }
    public void setEmailsProcessed(int emailsProcessed) { this.emailsProcessed = emailsProcessed; }

    public int getEventsExtracted() { return eventsExtracted; }
    public void setEventsExtracted(int eventsExtracted) { this.eventsExtracted = eventsExtracted; }

    public int getConflictsDetected() { return conflictsDetected; }
    public void setConflictsDetected(int conflictsDetected) { this.conflictsDetected = conflictsDetected; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
