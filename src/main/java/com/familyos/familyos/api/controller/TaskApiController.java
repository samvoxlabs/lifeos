package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.dto.TaskResponse;
import com.familyos.familyos.api.service.TaskApiService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    private final TaskApiService taskApiService;

    public TaskApiController(TaskApiService taskApiService) {
        this.taskApiService = taskApiService;
    }

    @GetMapping
    public PagedResponse<TaskResponse> getTasks(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        Pageable pageable
    ) {
        return taskApiService.getTasks(status, priority, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable UUID id) {
        return taskApiService.getTask(id);
    }
}
