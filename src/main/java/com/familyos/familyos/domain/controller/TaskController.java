package com.familyos.familyos.domain.controller;

import com.familyos.familyos.domain.dto.DomainActionDto;
import com.familyos.familyos.domain.service.DomainPersistenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final DomainPersistenceService domainPersistenceService;

    public TaskController(DomainPersistenceService domainPersistenceService) {
        this.domainPersistenceService = domainPersistenceService;
    }

    @GetMapping
    public List<DomainActionDto> getTasks() {
        return domainPersistenceService.getTasks();
    }
}
