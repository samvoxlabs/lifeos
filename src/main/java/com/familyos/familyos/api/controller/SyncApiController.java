package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.PopulateResponse;
import com.familyos.familyos.api.dto.StartResponse;
import com.familyos.familyos.api.dto.SyncSummaryResponse;
import com.familyos.familyos.api.service.StartupOrchestrationService;
import com.familyos.familyos.api.service.SyncOrchestrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SyncApiController {

    private final SyncOrchestrationService syncOrchestrationService;
    private final StartupOrchestrationService startupOrchestrationService;

    public SyncApiController(
        SyncOrchestrationService syncOrchestrationService,
        StartupOrchestrationService startupOrchestrationService
    ) {
        this.syncOrchestrationService = syncOrchestrationService;
        this.startupOrchestrationService = startupOrchestrationService;
    }

    @GetMapping("/start")
    public StartResponse start() {
        return startupOrchestrationService.start();
    }

    @GetMapping("/populate")
    public PopulateResponse populate(@RequestParam(defaultValue = "false") boolean useSeedData) throws Exception {
        return startupOrchestrationService.populate(useSeedData);
    }

    @PostMapping("/sync")
    public SyncSummaryResponse sync() {
        return syncOrchestrationService.syncAll();
    }
}
