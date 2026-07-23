package com.familyos.familyos.demo.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.mail.api.dto.MailConflictResolveResponse;
import com.familyos.familyos.mail.api.dto.MailResolutionRequest;
import com.familyos.familyos.mail.service.MailboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/conflicts")
public class DemoConflictsController {

    private final MailboxService mailboxService;
    private final AuthenticationService authenticationService;

    public DemoConflictsController(MailboxService mailboxService, AuthenticationService authenticationService) {
        this.mailboxService = mailboxService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<MailConflictResolveResponse>> resolve(
            @PathVariable String id,
            @RequestBody MailResolutionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String userId = authenticationService.currentUser().id();
        MailConflictResolveResponse response = mailboxService.resolveConflict(userId, id, request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
