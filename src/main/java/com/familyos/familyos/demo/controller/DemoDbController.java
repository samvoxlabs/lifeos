package com.familyos.familyos.demo.controller;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.repository.MailConflictRepository;
import com.familyos.familyos.mail.repository.MailExtractedEventRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo/db")
public class DemoDbController {

    private final MailExtractedEventRepository eventRepository;
    private final MailConflictRepository conflictRepository;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final AuthenticationService authenticationService;

    public DemoDbController(MailExtractedEventRepository eventRepository,
                            MailConflictRepository conflictRepository,
                            UserRepository userRepository,
                            OAuthAccountRepository oAuthAccountRepository,
                            AuthenticationService authenticationService) {
        this.eventRepository = eventRepository;
        this.conflictRepository = conflictRepository;
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> events() {
        String userId = authenticationService.currentUser().id();
        OAuthAccount account = resolveAccount(userId);
        List<MailExtractedEvent> events = eventRepository
                .findByMessageAccountOrderByStartsAtDesc(account, Pageable.ofSize(100));
        List<Map<String, Object>> result = events.stream().map(e -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", "event-" + e.getId());
            m.put("title", e.getTitle());
            m.put("start", e.getStartsAt() != null ? e.getStartsAt().toString() : null);
            m.put("end", e.getEndsAt() != null ? e.getEndsAt().toString() : null);
            m.put("status", e.getStatus());
            m.put("confidence", e.getConfidence());
            m.put("location", e.getLocation());
            m.put("sourceMessageId", e.getMessage() != null ? e.getMessage().getGmailMessageId() : null);
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @GetMapping("/conflicts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> conflicts() {
        String userId = authenticationService.currentUser().id();
        OAuthAccount account = resolveAccount(userId);
        List<MailExtractedEvent> events = eventRepository
                .findByMessageAccountOrderByStartsAtDesc(account, Pageable.ofSize(100));
        List<Map<String, Object>> result = events.stream()
                .flatMap(e -> conflictRepository.findByExtractedEvent(e).stream())
                .map(c -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", "conflict-" + c.getId());
                    m.put("status", c.getStatus());
                    m.put("extractedEventId", "event-" + c.getExtractedEvent().getId());
                    m.put("conflictingEventId", c.getConflictingEventId());
                    m.put("options", c.getSuggestedResolutions() != null ? c.getSuggestedResolutions().get("options") : List.of());
                    m.put("resolvedAt", c.getResolvedAt() != null ? c.getResolvedAt().toString() : null);
                    m.put("selectedOption", c.getAppliedResolutionKey());
                    return m;
                }).toList();
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    private OAuthAccount resolveAccount(String userId) {
        User user;
        try {
            user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId");
        }
        List<OAuthAccount> accounts = oAuthAccountRepository.findByUser(user);
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No OAuth account");
        }
        return accounts.get(0);
    }
}
