package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.dto.TaskItemDto;
import com.familyos.familyos.service.TasksService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/google-tasks")
public class TasksController {

    private final TasksService tasksService;
    private final AuthenticationService authenticationService;

    public TasksController(TasksService tasksService, AuthenticationService authenticationService) {
        this.tasksService = tasksService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/items")
    public List<TaskItemDto> tasks() {
        return tasksService.readTasks(authenticationService.currentUser().id());
    }
}
