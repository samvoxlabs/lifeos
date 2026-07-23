package com.familyos.familyos.demo.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.demo.dto.DemoResetResponse;
import com.familyos.familyos.demo.service.DemoGmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/gmail")
public class DemoGmailController {

    private final DemoGmailService demoGmailService;
    private final AuthenticationService authenticationService;

    public DemoGmailController(DemoGmailService demoGmailService,
                               AuthenticationService authenticationService) {
        this.demoGmailService = demoGmailService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<DemoResetResponse>> reset() {
        String userId = authenticationService.currentUser().id();
        int cleared = demoGmailService.resetDemoMailbox(userId);
        DemoResetResponse response = new DemoResetResponse("READY", "Demo inbox cleared and Gmail messages trashed (" + cleared + " emails removed)");
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PostMapping("/load")
    public ResponseEntity<ApiResponse<Map<String, Object>>> load(
            @RequestBody(required = false) Map<String, String> body) {
        String userId = authenticationService.currentUser().id();
        String file = body != null ? body.getOrDefault("file", "airport-medical-conflict.json") : "airport-medical-conflict.json";
        int count = demoGmailService.loadDemoEmails(userId, file);
        return ResponseEntity.ok(ApiResponse.of(Map.of("status", "READY", "emailsLoaded", count, "file", file)));
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> insert(
            @RequestBody Map<String, Object> body) {
        String userId = authenticationService.currentUser().id();
        Map<String, Object> response = demoGmailService.insertDemoEmail(userId, body);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> messages() {
        String userId = authenticationService.currentUser().id();
        List<Map<String, Object>> messages = demoGmailService.getGmailMessages(userId);
        return ResponseEntity.ok(ApiResponse.of(messages));
    }
}
