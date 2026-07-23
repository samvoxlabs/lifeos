package com.familyos.familyos.demo.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.demo.dto.DemoResetResponse;
import com.familyos.familyos.demo.dto.DemoSeedRequest;
import com.familyos.familyos.demo.dto.DemoSeedResponse;
import com.familyos.familyos.demo.service.DemoWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/demo", "/api/demo"})
public class DemoController {

    private final DemoWorkflowService demoWorkflowService;
    private final AuthenticationService authenticationService;

    public DemoController(DemoWorkflowService demoWorkflowService, AuthenticationService authenticationService) {
        this.demoWorkflowService = demoWorkflowService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<DemoResetResponse>> reset() {
        String userId = authenticationService.currentUser().id();
        DemoResetResponse response = demoWorkflowService.reset(userId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<DemoSeedResponse>> seed(@RequestBody(required = false) DemoSeedRequest request) {
        String userId = authenticationService.currentUser().id();
        DemoSeedResponse response = demoWorkflowService.seed(userId, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
