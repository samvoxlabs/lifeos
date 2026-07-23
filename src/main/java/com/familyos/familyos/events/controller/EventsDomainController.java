package com.familyos.familyos.events.controller;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.events.dto.EnrichedEventResponse;
import com.familyos.familyos.events.dto.EventSourceDto;
import com.familyos.familyos.events.dto.EventsListResponse;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.repository.MailExtractedEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/mail-events", "/api/mail-events"})
public class EventsDomainController {

    private final MailExtractedEventRepository eventRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;

    public EventsDomainController(MailExtractedEventRepository eventRepository,
                                  OAuthAccountRepository oAuthAccountRepository,
                                  UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EventsListResponse>> listEvents(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        OAuthAccount account = resolveAccount(userId);

        List<MailExtractedEvent> events;
        if (cursor != null && !cursor.isBlank()) {
            try {
                OffsetDateTime cursorTime = OffsetDateTime.parse(cursor);
                events = eventRepository.findByMessageAccountAndStartsAtLessThanOrderByStartsAtDesc(
                        account, cursorTime, PageRequest.of(0, limit));
            } catch (Exception e) {
                events = eventRepository.findByMessageAccountOrderByStartsAtDesc(account, PageRequest.of(0, limit));
            }
        } else {
            events = eventRepository.findByMessageAccountOrderByStartsAtDesc(account, PageRequest.of(0, limit));
        }

        List<EnrichedEventResponse> enriched = events.stream().map(this::toEnrichedResponse).toList();
        String nextCursor = events.size() == limit ? events.get(events.size() - 1).getStartsAt().toString() : null;
        return ResponseEntity.ok(ApiResponse.of(new EventsListResponse(enriched, enriched.size(), nextCursor)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrichedEventResponse>> getEvent(@PathVariable String id) {
        String rawId = id.startsWith("event-") ? id.substring("event-".length()) : id;
        try {
            UUID uuid = UUID.fromString(rawId);
            return eventRepository.findById(uuid)
                    .map(event -> ResponseEntity.ok(ApiResponse.of(toEnrichedResponse(event))))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private EnrichedEventResponse toEnrichedResponse(MailExtractedEvent event) {
        String title = event.getTitle() != null ? event.getTitle() : "";
        String[] typeCategory = deriveTypeAndCategory(title);
        String type = typeCategory[0];
        String category = typeCategory[1];
        String priority = derivePriority(title, type);

        String messageId = event.getMessage() != null ? event.getMessage().getGmailMessageId() : null;
        String threadId = event.getMessage() != null ? event.getMessage().getThreadId() : null;

        return new EnrichedEventResponse(
                "event-" + event.getId(),
                title,
                type,
                category,
                priority,
                event.getStartsAt() != null ? event.getStartsAt().toString() : null,
                event.getEndsAt() != null ? event.getEndsAt().toString() : null,
                event.getTimezone() != null ? event.getTimezone() : "America/Chicago",
                event.getLocation(),
                event.getEventDescription(),
                event.getConfidence(),
                event.getStatus(),
                event.getOwnerId(),
                new EventSourceDto("email", messageId, threadId)
        );
    }

    private String[] deriveTypeAndCategory(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("medical") || lower.contains("cardiology") || lower.contains("doctor")
                || lower.contains("hospital") || lower.contains("clinic") || lower.contains("dentist")
                || lower.contains("dental") || lower.contains("wellness") || lower.contains("vet")
                || lower.contains("veterinary")) {
            return new String[]{"medical", "health"};
        }
        if (lower.contains("pickup") || lower.contains("airport") || lower.contains("flight")
                || lower.contains("transport") || lower.contains("delivery") || lower.contains("drop-off")
                || lower.contains("drop off")) {
            return new String[]{"transportation", "logistics"};
        }
        if (lower.contains("school") || lower.contains("class") || lower.contains("lesson")
                || lower.contains("conference") || lower.contains("teacher") || lower.contains("pta")
                || lower.contains("orientation") || lower.contains("piano") || lower.contains("training")) {
            return new String[]{"education", "family"};
        }
        if (lower.contains("soccer") || lower.contains("sport") || lower.contains("practice")
                || lower.contains("game") || lower.contains("gym")) {
            return new String[]{"sports", "recreation"};
        }
        if (lower.contains("dinner") || lower.contains("lunch") || lower.contains("breakfast")
                || lower.contains("restaurant") || lower.contains("reservation")) {
            return new String[]{"dining", "recreation"};
        }
        if (lower.contains("pet") || lower.contains("buddy") || lower.contains("grooming")
                || lower.contains("boarding") || lower.contains("obedience")) {
            return new String[]{"pet", "family"};
        }
        return new String[]{"general", "family"};
    }

    private String derivePriority(String title, String type) {
        String lower = title.toLowerCase();
        if ("medical".equals(type) || lower.contains("urgent") || lower.contains("emergency")
                || lower.contains("school") || lower.contains("conference") || lower.contains("pickup")) {
            return "high";
        }
        if ("transportation".equals(type) || "education".equals(type)) {
            return "medium";
        }
        return "medium";
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
