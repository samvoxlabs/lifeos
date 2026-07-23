package com.familyos.familyos.workflow.repository;

import com.familyos.familyos.workflow.entity.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {
    List<WorkflowExecution> findByTriggeredByUserIdOrderByStartedAtDesc(String userId);
    Optional<WorkflowExecution> findByIdAndTriggeredByUserId(UUID id, String userId);
}
