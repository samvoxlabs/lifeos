package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.SyncSummaryResponse;
import com.familyos.familyos.api.service.SyncOrchestrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SyncApiController {

    private final SyncOrchestrationService syncOrchestrationService;

    public SyncApiController(SyncOrchestrationService syncOrchestrationService) {
        this.syncOrchestrationService = syncOrchestrationService;
    }

    @PostMapping("/sync")
    public SyncSummaryResponse sync() {
        return syncOrchestrationService.syncAll();
    }
}
