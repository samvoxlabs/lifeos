package com.familyos.familyos.domain.controller;

import com.familyos.familyos.domain.dto.DomainActionDto;
import com.familyos.familyos.domain.service.DomainPersistenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final DomainPersistenceService domainPersistenceService;

    public ReminderController(DomainPersistenceService domainPersistenceService) {
        this.domainPersistenceService = domainPersistenceService;
    }

    @GetMapping
    public List<DomainActionDto> getReminders() {
        return domainPersistenceService.getReminders();
    }
}
