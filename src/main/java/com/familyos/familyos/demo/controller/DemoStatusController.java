package com.familyos.familyos.demo.controller;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.repository.MailConflictRepository;
import com.familyos.familyos.mail.repository.MailExtractedEventRepository;
import com.familyos.familyos.mail.repository.MailMessageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo")
public class DemoStatusController {

    private final MailMessageRepository mailMessageRepository;
    private final MailExtractedEventRepository mailExtractedEventRepository;
    private final MailConflictRepository mailConflictRepository;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final AuthenticationService authenticationService;

    public DemoStatusController(MailMessageRepository mailMessageRepository,
                                MailExtractedEventRepository mailExtractedEventRepository,
                                MailConflictRepository mailConflictRepository,
                                UserRepository userRepository,
                                OAuthAccountRepository oAuthAccountRepository,
                                AuthenticationService authenticationService) {
        this.mailMessageRepository = mailMessageRepository;
        this.mailExtractedEventRepository = mailExtractedEventRepository;
        this.mailConflictRepository = mailConflictRepository;
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        String userId = authenticationService.currentUser().id();
        OAuthAccount account = resolveAccount(userId);

        long gmailCount = mailMessageRepository.findByAccountOrderByReceivedAtDesc(account, Pageable.ofSize(1000)).size();

        List<MailExtractedEvent> events =
                mailExtractedEventRepository.findByMessageAccountOrderByStartsAtDesc(account, Pageable.ofSize(1000));
        long eventCount = events.size();
        long conflictCount = events.stream()
                .flatMap(e -> mailConflictRepository.findByExtractedEvent(e).stream())
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gmailMessageCount", gmailCount);
        result.put("extractedEventCount", eventCount);
        result.put("conflictCount", conflictCount);
        result.put("publishedEventCount", events.stream().filter(e -> "published".equals(e.getStatus())).count());
        result.put("readyToPublishCount", events.stream().filter(e -> "ready_to_publish".equals(e.getStatus())).count());

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
