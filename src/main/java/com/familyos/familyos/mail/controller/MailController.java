package com.familyos.familyos.mail.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.mail.api.dto.MailConflictResolveResponse;
import com.familyos.familyos.mail.api.dto.MailMessagePatchRequest;
import com.familyos.familyos.mail.api.dto.MailMessageResponse;
import com.familyos.familyos.mail.api.dto.MailMessagesPageResponse;
import com.familyos.familyos.mail.api.dto.MailResolutionRequest;
import com.familyos.familyos.mail.api.dto.MailSyncRequest;
import com.familyos.familyos.mail.api.dto.MailSyncResponse;
import com.familyos.familyos.mail.api.dto.ReviewScheduleRequest;
import com.familyos.familyos.mail.api.dto.ReviewScheduleResponse;
import com.familyos.familyos.mail.service.MailboxService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/mail", "/api/mail"})
public class MailController {

    private final MailboxService mailboxService;
    private final AuthenticationService authenticationService;

    public MailController(MailboxService mailboxService, AuthenticationService authenticationService) {
        this.mailboxService = mailboxService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/sync")
    public MailSyncResponse sync(@Valid @RequestBody(required = false) MailSyncRequest request) {
        return mailboxService.syncMailbox(authenticationService.currentUser().id(), request);
    }

    @GetMapping("/messages")
    public MailMessagesPageResponse getMessages(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return mailboxService.listMessages(authenticationService.currentUser().id(), limit, cursor);
    }

    @GetMapping("/messages/{messageId}")
    public MailMessageResponse getMessage(@PathVariable String messageId) {
        return mailboxService.getMessage(authenticationService.currentUser().id(), messageId);
    }

    @PatchMapping("/messages/{messageId}")
    public MailMessageResponse updateMessage(
            @PathVariable String messageId,
            @Valid @RequestBody MailMessagePatchRequest request
    ) {
        return mailboxService.updateMessage(authenticationService.currentUser().id(), messageId, request);
    }

    @PostMapping("/conflicts/{conflictId}/resolve")
    public MailConflictResolveResponse resolveConflict(
            @PathVariable String conflictId,
            @Valid @RequestBody MailResolutionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return mailboxService.resolveConflict(authenticationService.currentUser().id(), conflictId, request, idempotencyKey);
    }

    @PostMapping("/review-schedule")
    public ResponseEntity<ReviewScheduleResponse> reviewSchedule(
            @RequestBody(required = false) ReviewScheduleRequest request
    ) {
        String userId = authenticationService.currentUser().id();
        ReviewScheduleResponse response = mailboxService.reviewSchedule(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/review")
    public ResponseEntity<ReviewScheduleResponse> review(
            @RequestBody(required = false) ReviewScheduleRequest request
    ) {
        String userId = authenticationService.currentUser().id();
        ReviewScheduleResponse response = mailboxService.reviewSchedule(userId, request);
        return ResponseEntity.ok(response);
    }
}
