package com.familyos.familyos.conflicts.controller;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.conflicts.dto.ConflictResolveRequest;
import com.familyos.familyos.conflicts.dto.ConflictResolveResponse;
import com.familyos.familyos.mail.api.dto.MailConflictResolveResponse;
import com.familyos.familyos.mail.api.dto.MailResolutionRequest;
import com.familyos.familyos.mail.entity.MailConflict;
import com.familyos.familyos.mail.repository.MailConflictRepository;
import com.familyos.familyos.mail.service.MailboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/conflicts", "/api/conflicts"})
public class ConflictsDomainController {

    private final MailConflictRepository conflictRepository;
    private final MailboxService mailboxService;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;

    public ConflictsDomainController(MailConflictRepository conflictRepository,
                                     MailboxService mailboxService,
                                     OAuthAccountRepository oAuthAccountRepository,
                                     UserRepository userRepository) {
        this.conflictRepository = conflictRepository;
        this.mailboxService = mailboxService;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listConflicts(
            @RequestParam(required = false) String status) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        OAuthAccount account = resolveAccount(userId);

        List<MailConflict> conflicts;
        if (status != null && !status.isBlank()) {
            conflicts = conflictRepository.findByExtractedEventMessageAccountAndStatus(account, status);
        } else {
            conflicts = conflictRepository.findByExtractedEventMessageAccount(account);
        }

        List<Map<String, Object>> enriched = conflicts.stream().map(this::toEnrichedConflict).toList();
        return ResponseEntity.ok(ApiResponse.of(enriched));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ConflictResolveResponse>> resolveConflict(
            @PathVariable String id,
            @RequestBody ConflictResolveRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String conflictId = id.startsWith("conflict-") ? id : "conflict-" + id;

        MailResolutionRequest resolutionRequest = new MailResolutionRequest(request.optionId(), null, null);
        String idempotencyKey = "conflicts-api-" + conflictId + "-" + request.optionId();

        MailConflictResolveResponse resolveResponse = mailboxService.resolveConflict(
                userId, conflictId, resolutionRequest, idempotencyKey);

        ConflictResolveResponse response = new ConflictResolveResponse(
                resolveResponse.conflictId(),
                resolveResponse.status(),
                resolveResponse.selectedOptionId(),
                resolveResponse.resolvedAt() != null ? resolveResponse.resolvedAt().toString() : null
        );
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toEnrichedConflict(MailConflict conflict) {
        List<Map<String, Object>> enrichedOptions = new ArrayList<>();
        Map<String, Object> resolutions = conflict.getSuggestedResolutions();
        if (resolutions != null && resolutions.get("options") instanceof List<?> options) {
            for (Object opt : options) {
                if (opt instanceof Map<?, ?> optMap) {
                    enrichedOptions.add((Map<String, Object>) optMap);
                }
            }
        }

        String extractedEventId = conflict.getExtractedEvent() != null
                ? "event-" + conflict.getExtractedEvent().getId() : null;

        return Map.of(
                "id", "conflict-" + conflict.getId(),
                "type", "time_overlap",
                "title", "Schedule clash detected",
                "description", "Two events overlap in time and require resolution.",
                "eventIds", extractedEventId != null
                        ? List.of(extractedEventId, conflict.getConflictingEventId())
                        : List.of(conflict.getConflictingEventId()),
                "status", conflict.getStatus() != null ? conflict.getStatus() : "open",
                "overlapStart", conflict.getOverlapStart() != null ? conflict.getOverlapStart().toString() : null,
                "overlapEnd", conflict.getOverlapEnd() != null ? conflict.getOverlapEnd().toString() : null,
                "options", enrichedOptions
        );
    }

    private OAuthAccount resolveAccount(String userId) {
        User user;
        try {
            user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid user: " + userId);
        }
        List<OAuthAccount> accounts = oAuthAccountRepository.findByUser(user);
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No OAuth account for user: " + userId);
        }
        return accounts.get(0);
    }
}
