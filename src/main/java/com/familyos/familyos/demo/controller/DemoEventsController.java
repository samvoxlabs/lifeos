package com.familyos.familyos.demo.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.mail.api.dto.ReviewScheduleRequest;
import com.familyos.familyos.mail.api.dto.ReviewScheduleResponse;
import com.familyos.familyos.mail.service.MailboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/events")
public class DemoEventsController {

    private final MailboxService mailboxService;
    private final AuthenticationService authenticationService;

    public DemoEventsController(MailboxService mailboxService, AuthenticationService authenticationService) {
        this.mailboxService = mailboxService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<ReviewScheduleResponse>> extract(
            @RequestBody(required = false) ReviewScheduleRequest request) {
        String userId = authenticationService.currentUser().id();
        ReviewScheduleResponse response = mailboxService.reviewSchedule(userId, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
