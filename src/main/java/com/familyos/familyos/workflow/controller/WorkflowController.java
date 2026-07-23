package com.familyos.familyos.workflow.controller;

import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.workflow.dto.WorkflowApproveRequest;
import com.familyos.familyos.workflow.dto.WorkflowApproveResponse;
import com.familyos.familyos.workflow.dto.WorkflowStartResponse;
import com.familyos.familyos.workflow.dto.WorkflowStatusResponse;
import com.familyos.familyos.workflow.entity.WorkflowExecution;
import com.familyos.familyos.workflow.repository.WorkflowExecutionRepository;
import com.familyos.familyos.workflow.service.EmailWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping({"/workflows", "/api/workflows"})
public class WorkflowController {

    private final EmailWorkflowService emailWorkflowService;
    private final WorkflowExecutionRepository workflowExecutionRepository;

    public WorkflowController(EmailWorkflowService emailWorkflowService,
                              WorkflowExecutionRepository workflowExecutionRepository) {
        this.emailWorkflowService = emailWorkflowService;
        this.workflowExecutionRepository = workflowExecutionRepository;
    }

    @PostMapping("/email-sync")
    public ResponseEntity<ApiResponse<WorkflowStartResponse>> startEmailSync() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        WorkflowExecution execution = new WorkflowExecution();
        execution.setTriggeredByUserId(userId);
        execution.setStatus("RUNNING");
        execution.setCurrentStep("initializing");
        execution.setStartedAt(OffsetDateTime.now());
        WorkflowExecution saved = workflowExecutionRepository.save(execution);

        emailWorkflowService.executeEmailSyncWorkflow(saved.getId(), userId);

        WorkflowStartResponse response = new WorkflowStartResponse(
                saved.getId().toString(),
                saved.getStatus(),
                "Email sync workflow started"
        );
        return ResponseEntity.accepted().body(ApiResponse.of(response));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<ApiResponse<WorkflowStatusResponse>> getStatus(@PathVariable String workflowId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            UUID id = UUID.fromString(workflowId);
            WorkflowStatusResponse status = emailWorkflowService.getStatus(id, userId);
            return ResponseEntity.ok(ApiResponse.of(status));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{workflowId}/approve")
    public ResponseEntity<ApiResponse<WorkflowApproveResponse>> approve(
            @PathVariable String workflowId,
            @RequestBody WorkflowApproveRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            UUID id = UUID.fromString(workflowId);
            WorkflowApproveResponse response = emailWorkflowService.approve(id, userId, request);
            return ResponseEntity.ok(ApiResponse.of(response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
