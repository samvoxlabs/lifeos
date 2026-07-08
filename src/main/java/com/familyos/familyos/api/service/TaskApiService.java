package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.dto.TaskResponse;
import com.familyos.familyos.api.exception.ApiNotFoundException;
import com.familyos.familyos.api.exception.ApiValidationException;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.entity.Task;
import com.familyos.familyos.domain.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@Service
public class TaskApiService {

    private final TaskRepository taskRepository;
    private final ApiDtoMapper apiDtoMapper;

    public TaskApiService(TaskRepository taskRepository, ApiDtoMapper apiDtoMapper) {
        this.taskRepository = taskRepository;
        this.apiDtoMapper = apiDtoMapper;
    }

    public PagedResponse<TaskResponse> getTasks(String status, String priority, Pageable pageable) {
        validatePriority(priority);
        Page<TaskResponse> page = taskRepository.findAll(taskSpecification(status, priority), pageable)
            .map(apiDtoMapper::toTaskResponse);
        return PagedResponse.from(page, pageable.getSort().toString());
    }

    public TaskResponse getTask(UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ApiNotFoundException("Task not found: " + id));
        return apiDtoMapper.toTaskResponse(task);
    }

    private Specification<Task> taskSpecification(String status, String priority) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase(Locale.ROOT)));
            }
            if (priority != null && !priority.isBlank()) {
                String normalized = priority.trim().toUpperCase(Locale.ROOT);
                switch (normalized) {
                    case "HIGH" -> predicates.add(cb.greaterThanOrEqualTo(root.get("confidence"), 0.8d));
                    case "MEDIUM" -> predicates.add(cb.and(
                        cb.greaterThanOrEqualTo(root.get("confidence"), 0.5d),
                        cb.lessThan(root.get("confidence"), 0.8d)
                    ));
                    case "LOW" -> predicates.add(cb.lessThan(root.get("confidence"), 0.5d));
                    default -> {
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validatePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return;
        }
        String normalized = priority.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("HIGH") && !normalized.equals("MEDIUM") && !normalized.equals("LOW")) {
            throw new ApiValidationException("Invalid task priority filter: " + priority);
        }
    }
}
