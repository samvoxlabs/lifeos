package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.SyncSummaryResponse;
import com.familyos.familyos.api.service.SyncOrchestrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/google/sync")
public class GoogleSyncController {

    private final SyncOrchestrationService syncOrchestrationService;

    public GoogleSyncController(SyncOrchestrationService syncOrchestrationService) {
        this.syncOrchestrationService = syncOrchestrationService;
    }

    @PostMapping("/gmail")
    public SyncSummaryResponse syncGmail() {
        return syncOrchestrationService.syncGmail();
    }

    @PostMapping("/calendar")
    public SyncSummaryResponse syncCalendar() {
        return syncOrchestrationService.syncCalendar();
    }

    @PostMapping("/drive")
    public SyncSummaryResponse syncDrive() {
        return syncOrchestrationService.syncDrive();
    }
}
