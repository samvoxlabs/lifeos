package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.dto.ReminderResponse;
import com.familyos.familyos.api.service.ReminderApiService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
public class ReminderApiController {

    private final ReminderApiService reminderApiService;

    public ReminderApiController(ReminderApiService reminderApiService) {
        this.reminderApiService = reminderApiService;
    }

    @GetMapping
    public PagedResponse<ReminderResponse> getReminders(
        @RequestParam(required = false) String status,
        Pageable pageable
    ) {
        return reminderApiService.getReminders(status, pageable);
    }

    @GetMapping("/{id}")
    public ReminderResponse getReminder(@PathVariable UUID id) {
        return reminderApiService.getReminder(id);
    }
}
