package com.familyos.familyos.workflow.service;

import com.familyos.familyos.mail.api.dto.MailConflictResolveResponse;
import com.familyos.familyos.mail.api.dto.MailResolutionRequest;
import com.familyos.familyos.mail.api.dto.MailSyncRequest;
import com.familyos.familyos.mail.api.dto.MailSyncResponse;
import com.familyos.familyos.mail.service.MailboxService;
import com.familyos.familyos.workflow.dto.WorkflowApproveRequest;
import com.familyos.familyos.workflow.dto.WorkflowApproveResponse;
import com.familyos.familyos.workflow.dto.WorkflowStatusResponse;
import com.familyos.familyos.workflow.entity.WorkflowExecution;
import com.familyos.familyos.workflow.repository.WorkflowExecutionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(EmailWorkflowService.class);

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final MailboxService mailboxService;

    public EmailWorkflowService(WorkflowExecutionRepository workflowExecutionRepository,
                                MailboxService mailboxService) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.mailboxService = mailboxService;
    }

    @Async("workflowExecutor")
    public CompletableFuture<Void> executeEmailSyncWorkflow(UUID workflowId, String userId) {
        log.info("Starting email sync workflow {} for user {}", workflowId, userId);
        try {
            updateWorkflow(workflowId, "RUNNING", "gmail_sync", 0, 0, 0, null, null);

            MailSyncRequest syncRequest = new MailSyncRequest(null, null, 50);
            MailSyncResponse syncResponse = mailboxService.syncMailbox(userId, syncRequest);

            int emailsProcessed = syncResponse.messages() == null ? 0 : syncResponse.messages().size();
            updateWorkflow(workflowId, "RUNNING", "event_extraction", emailsProcessed, 0, 0, null, null);

            int eventsExtracted = syncResponse.events() == null ? 0 : syncResponse.events().size();
            updateWorkflow(workflowId, "RUNNING", "conflict_detection", emailsProcessed, eventsExtracted, 0, null, null);

            int conflictsDetected = syncResponse.conflicts() == null ? 0 : syncResponse.conflicts().size();

            if (conflictsDetected > 0) {
                updateWorkflow(workflowId, "WAITING_FOR_USER", "waiting_for_approval",
                        emailsProcessed, eventsExtracted, conflictsDetected, null, null);
            } else {
                updateWorkflow(workflowId, "COMPLETED", "completed",
                        emailsProcessed, eventsExtracted, conflictsDetected, null, OffsetDateTime.now());
            }
            log.info("Email sync workflow {} completed. emails={}, events={}, conflicts={}",
                    workflowId, emailsProcessed, eventsExtracted, conflictsDetected);
        } catch (Exception ex) {
            log.error("Email sync workflow {} failed for user {}: {}", workflowId, userId, ex.getMessage(), ex);
            updateWorkflow(workflowId, "FAILED", "error", 0, 0, 0, ex.getMessage(), OffsetDateTime.now());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public WorkflowStatusResponse getStatus(UUID workflowId, String userId) {
        WorkflowExecution execution = workflowExecutionRepository
                .findByIdAndTriggeredByUserId(workflowId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        return toStatusResponse(execution);
    }

    @Transactional
    public WorkflowApproveResponse approve(UUID workflowId, String userId, WorkflowApproveRequest request) {
        WorkflowExecution execution = workflowExecutionRepository
                .findByIdAndTriggeredByUserId(workflowId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));

        MailResolutionRequest resolutionRequest = new MailResolutionRequest(request.optionId(), null, null);
        String idempotencyKey = "workflow-" + workflowId + "-conflict-" + request.conflictId();
        MailConflictResolveResponse resolveResponse = mailboxService.resolveConflict(
                userId, request.conflictId(), resolutionRequest, idempotencyKey);

        execution.setStatus("COMPLETED");
        execution.setCurrentStep("resolved");
        execution.setCompletedAt(OffsetDateTime.now());
        workflowExecutionRepository.save(execution);

        return new WorkflowApproveResponse(
                workflowId.toString(),
                "COMPLETED",
                resolveResponse.conflictId(),
                resolveResponse.selectedOptionId(),
                true,
                resolveResponse.resolvedAt() == null ? OffsetDateTime.now().toString() : resolveResponse.resolvedAt().toString()
        );
    }

    @Transactional
    protected void updateWorkflow(UUID workflowId, String status, String currentStep,
                                  int emailsProcessed, int eventsExtracted, int conflictsDetected,
                                  String errorMessage, OffsetDateTime completedAt) {
        workflowExecutionRepository.findById(workflowId).ifPresent(execution -> {
            execution.setStatus(status);
            execution.setCurrentStep(currentStep);
            execution.setEmailsProcessed(emailsProcessed);
            execution.setEventsExtracted(eventsExtracted);
            execution.setConflictsDetected(conflictsDetected);
            execution.setErrorMessage(errorMessage);
            execution.setCompletedAt(completedAt);
            workflowExecutionRepository.save(execution);
        });
    }

    private WorkflowStatusResponse toStatusResponse(WorkflowExecution execution) {
        return new WorkflowStatusResponse(
                execution.getId().toString(),
                execution.getStatus(),
                execution.getCurrentStep(),
                execution.getEmailsProcessed(),
                execution.getEventsExtracted(),
                execution.getConflictsDetected(),
                execution.getErrorMessage(),
                execution.getStartedAt() == null ? null : execution.getStartedAt().toString(),
                execution.getCompletedAt() == null ? null : execution.getCompletedAt().toString()
        );
    }
}
