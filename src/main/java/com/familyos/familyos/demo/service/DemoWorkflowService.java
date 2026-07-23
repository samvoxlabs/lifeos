package com.familyos.familyos.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import com.familyos.familyos.demo.dto.DemoResetResponse;
import com.familyos.familyos.demo.dto.DemoSeedRequest;
import com.familyos.familyos.demo.dto.DemoSeedResponse;
import com.familyos.familyos.mail.entity.MailConflict;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.entity.MailMessage;
import com.familyos.familyos.mail.repository.MailConflictRepository;
import com.familyos.familyos.mail.repository.MailExtractedEventRepository;
import com.familyos.familyos.mail.repository.MailMessageRepository;
import com.familyos.familyos.mail.repository.MailResolutionExecutionRepository;
import com.familyos.familyos.mail.repository.MailSyncStateRepository;
import com.familyos.familyos.workflow.entity.WorkflowExecution;
import com.familyos.familyos.workflow.repository.WorkflowExecutionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DemoWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(DemoWorkflowService.class);
    private static final String DEFAULT_SCENARIO = "airport-medical-conflict";
    private static final String DEFAULT_OWNER_ID = "mom";
    private static final String DEFAULT_TIMEZONE = "America/Chicago";

    private final MailMessageRepository mailMessageRepository;
    private final MailExtractedEventRepository mailExtractedEventRepository;
    private final MailConflictRepository mailConflictRepository;
    private final MailResolutionExecutionRepository mailResolutionExecutionRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final MailSyncStateRepository mailSyncStateRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DemoWorkflowService(MailMessageRepository mailMessageRepository,
                               MailExtractedEventRepository mailExtractedEventRepository,
                               MailConflictRepository mailConflictRepository,
                               MailResolutionExecutionRepository mailResolutionExecutionRepository,
                               WorkflowExecutionRepository workflowExecutionRepository,
                               MailSyncStateRepository mailSyncStateRepository,
                               OAuthAccountRepository oAuthAccountRepository,
                               UserRepository userRepository,
                               ObjectMapper objectMapper) {
        this.mailMessageRepository = mailMessageRepository;
        this.mailExtractedEventRepository = mailExtractedEventRepository;
        this.mailConflictRepository = mailConflictRepository;
        this.mailResolutionExecutionRepository = mailResolutionExecutionRepository;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.mailSyncStateRepository = mailSyncStateRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DemoResetResponse reset(String userId) {
        log.info("Demo reset requested for user {}", userId);
        OAuthAccount account = resolveAccount(userId);

        // Delete resolution executions -> conflicts -> extracted events -> messages
        List<MailMessage> messages = mailMessageRepository.findByAccountOrderByReceivedAtDesc(account, Pageable.unpaged());
        for (MailMessage message : messages) {
            mailExtractedEventRepository.findByMessage(message).ifPresent(event -> {
                List<MailConflict> conflicts = mailConflictRepository.findByExtractedEvent(event);
                for (MailConflict conflict : conflicts) {
                    mailResolutionExecutionRepository.findByConflictAndActionKey(conflict, conflict.getAppliedResolutionKey() != null ? conflict.getAppliedResolutionKey() : "")
                            .ifPresent(mailResolutionExecutionRepository::delete);
                    mailConflictRepository.deleteById(conflict.getId());
                }
                mailExtractedEventRepository.delete(event);
            });
            mailMessageRepository.delete(message);
        }

        // Delete sync state for account
        mailSyncStateRepository.findByAccount(account).ifPresent(mailSyncStateRepository::delete);

        // Delete workflow executions for this user
        List<WorkflowExecution> executions = workflowExecutionRepository.findByTriggeredByUserIdOrderByStartedAtDesc(userId);
        workflowExecutionRepository.deleteAll(executions);

        // Seed inbox with demo messages (no events or conflicts — those are created by /mail/review-schedule)
        List<Map<String, Object>> demoMessages = loadScenarioJson(DEFAULT_SCENARIO);
        int count = 0;
        for (Map<String, Object> msgData : demoMessages) {
            createMailMessage(account, msgData);
            count++;
        }

        log.info("Demo reset complete: {} emails seeded for user {}", count, userId);
        return new DemoResetResponse("READY", "Demo inbox seeded with " + count + " emails");
    }

    @Transactional
    public DemoSeedResponse seed(String userId, DemoSeedRequest request) {
        String scenario = (request == null || request.scenario() == null || request.scenario().isBlank())
                ? DEFAULT_SCENARIO : request.scenario();
        log.info("Demo seed requested for user {} with scenario {}", userId, scenario);

        OAuthAccount account = resolveAccount(userId);
        List<Map<String, Object>> demoMessages = loadScenarioJson(scenario);

        // Map of demo message id -> MailExtractedEvent for conflict linking
        Map<String, MailExtractedEvent> createdEvents = new LinkedHashMap<>();

        int count = 0;
        for (Map<String, Object> msgData : demoMessages) {
            MailMessage message = createMailMessage(account, msgData);

            @SuppressWarnings("unchecked")
            Map<String, Object> extractedEventData = (Map<String, Object>) msgData.get("extractedEvent");
            if (extractedEventData != null) {
                MailExtractedEvent event = createMailExtractedEvent(message, extractedEventData);
                message.setHasActionableEvent(true);
                mailMessageRepository.save(message);
                createdEvents.put((String) msgData.get("id"), event);
            }
            count++;
        }

        // Detect and create conflicts for airport-medical-conflict scenario
        createConflictsIfPresent(demoMessages, createdEvents);

        log.info("Demo seed complete: {} emails loaded for scenario {}", count, scenario);
        return new DemoSeedResponse("READY", count, scenario);
    }

    @SuppressWarnings("unchecked")
    private void createConflictsIfPresent(List<Map<String, Object>> demoMessages,
                                          Map<String, MailExtractedEvent> createdEvents) {
        for (Map<String, Object> msgData : demoMessages) {
            Map<String, Object> extractedEventData = (Map<String, Object>) msgData.get("extractedEvent");
            if (extractedEventData == null) continue;

            String conflictsWith = (String) extractedEventData.get("conflictsWith");
            if (conflictsWith == null) continue;

            String msgId = (String) msgData.get("id");
            MailExtractedEvent event = createdEvents.get(msgId);
            MailExtractedEvent conflictingEvent = createdEvents.get(conflictsWith);
            if (event == null || conflictingEvent == null) continue;

            // Create conflict: this event conflicts with the other
            MailConflict conflict = new MailConflict();
            conflict.setExtractedEvent(event);
            conflict.setConflictingEventId("event-" + conflictingEvent.getId());
            conflict.setConflictingEventSource("mail");
            conflict.setOverlapStart(event.getStartsAt());
            conflict.setOverlapEnd(conflictingEvent.getEndsAt());
            conflict.setStatus("open");
            conflict.setSuggestedResolutions(buildEnrichedConflictOptions(event));
            mailConflictRepository.save(conflict);

            // Mark both events as conflicting
            event.setStatus("conflict");
            mailExtractedEventRepository.save(event);

            log.info("Created conflict between events {} and {}", event.getId(), conflictingEvent.getId());
        }
    }

    private MailMessage createMailMessage(OAuthAccount account, Map<String, Object> msgData) {
        MailMessage message = new MailMessage();
        message.setAccount(account);
        message.setGmailMessageId((String) msgData.get("gmailMessageId"));

        @SuppressWarnings("unchecked")
        Map<String, Object> from = (Map<String, Object>) msgData.get("from");
        if (from != null) {
            message.setSenderName((String) from.get("name"));
            message.setSenderEmail((String) from.get("email"));
        }

        @SuppressWarnings("unchecked")
        List<String> to = (List<String>) msgData.get("to");
        message.setRecipients(to);
        message.setSubject((String) msgData.get("subject"));
        message.setSnippet((String) msgData.get("snippet"));
        message.setBodyText((String) msgData.get("bodyText"));

        String receivedAt = (String) msgData.get("receivedAt");
        message.setReceivedAt(receivedAt != null ? OffsetDateTime.parse(receivedAt) : OffsetDateTime.now());

        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) msgData.get("labels");
        message.setLabels(labels);
        message.setHasActionableEvent(false);
        message.setRead(false);

        return mailMessageRepository.save(message);
    }

    private MailExtractedEvent createMailExtractedEvent(MailMessage message, Map<String, Object> data) {
        MailExtractedEvent event = new MailExtractedEvent();
        event.setMessage(message);
        event.setTitle((String) data.get("title"));

        String start = (String) data.get("start");
        String end = (String) data.get("end");
        OffsetDateTime startsAt = start != null ? OffsetDateTime.parse(start) : OffsetDateTime.now();
        OffsetDateTime endsAt = end != null ? OffsetDateTime.parse(end) : startsAt.plusHours(1);
        event.setStartsAt(startsAt);
        event.setEndsAt(endsAt);
        event.setTimezoneOffset(startsAt.getOffset().getId());

        String timezone = (String) data.get("timezone");
        event.setTimezone(timezone != null ? timezone : DEFAULT_TIMEZONE);
        event.setLocation((String) data.get("location"));
        event.setEventDescription((String) data.get("description"));
        event.setOwnerId(DEFAULT_OWNER_ID);

        Object conf = data.get("confidence");
        event.setConfidence(conf instanceof Number n ? n.doubleValue() : 0.90);
        event.setStatus("confirmed");

        return mailExtractedEventRepository.save(event);
    }

    private Map<String, Object> buildEnrichedConflictOptions(MailExtractedEvent event) {
        String eventId = "event-" + event.getId();

        List<Map<String, Object>> agenticStepsAssign = List.of(
                Map.of("service", "calendar", "action", "update_event", "detail", "Reassign event owner"),
                Map.of("service", "gmail", "action", "draft_email", "detail", "Draft notification to assignee"),
                Map.of("service", "notifications", "action", "send_push", "detail", "Push notification to family member")
        );
        Map<String, Object> assignAgenticPlan = Map.of(
                "summary", "Reassign airport pickup to an available family member and send notification.",
                "steps", agenticStepsAssign
        );
        Map<String, Object> assignEffects = new LinkedHashMap<>();
        assignEffects.put("eventId", eventId);
        assignEffects.put("assigneeId", "dad");
        assignEffects.put("notifyMemberIds", List.of("dad", "child"));

        List<Map<String, Object>> agenticStepsDelay = List.of(
                Map.of("service", "calendar", "action", "update_event", "detail", "Update event start and end times"),
                Map.of("service", "gmail", "action", "draft_email", "detail", "Draft delay notification"),
                Map.of("service", "notifications", "action", "send_push", "detail", "Push notification to child")
        );
        Map<String, Object> delayAgenticPlan = Map.of(
                "summary", "Delay airport pickup by 1 hour and notify the arriving family member.",
                "steps", agenticStepsDelay
        );
        Map<String, Object> delayEffects = new LinkedHashMap<>();
        delayEffects.put("eventId", eventId);
        delayEffects.put("newStart", event.getStartsAt().plusHours(1).toString());
        delayEffects.put("newEnd", event.getEndsAt().plusHours(1).toString());
        delayEffects.put("notifyMemberIds", List.of("child"));

        List<Map<String, Object>> agenticStepsCancel = List.of(
                Map.of("service", "calendar", "action", "delete_event", "detail", "Remove pickup event"),
                Map.of("service", "notifications", "action", "send_push", "detail", "Notify all family members")
        );
        Map<String, Object> cancelAgenticPlan = Map.of(
                "summary", "Cancel airport pickup event and notify all affected family members.",
                "steps", agenticStepsCancel
        );
        Map<String, Object> cancelEffects = Map.of(
                "eventId", eventId,
                "notifyMemberIds", List.of("child", "dad")
        );

        List<Map<String, Object>> options = new ArrayList<>();
        options.add(Map.of(
                "id", "assign-family-member",
                "label", "Ask another family member",
                "description", "Assign airport pickup to an available family member.",
                "score", 0.94,
                "reason", "Medical appointment cannot be moved; delegating pickup preserves both commitments.",
                "recommended", true,
                "agenticPlan", assignAgenticPlan,
                "effects", assignEffects
        ));
        options.add(Map.of(
                "id", "delay-pickup",
                "label", "Delay pickup 1 hour",
                "description", "Move pickup to 1 hour later and notify the arriving family member.",
                "score", 0.76,
                "reason", "Delaying pickup is feasible but may inconvenience the arriving traveler.",
                "recommended", false,
                "agenticPlan", delayAgenticPlan,
                "effects", delayEffects
        ));
        options.add(Map.of(
                "id", "cancel-pickup",
                "label", "Cancel airport pickup",
                "description", "Cancel the airport pickup and arrange alternative transport.",
                "score", 0.45,
                "reason", "Cancellation is a last resort and may inconvenience the arriving traveler.",
                "recommended", false,
                "agenticPlan", cancelAgenticPlan,
                "effects", cancelEffects
        ));

        return Map.of("options", options);
    }

    private OAuthAccount resolveAccount(String userId) {
        User user;
        try {
            user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid user id: " + userId);
        }
        List<OAuthAccount> accounts = oAuthAccountRepository.findByUser(user);
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No OAuth account found for user: " + userId);
        }
        return accounts.get(0);
    }

    private List<Map<String, Object>> loadScenarioJson(String scenario) {
        String path = "demo/" + scenario + ".json";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (IOException ex) {
            throw new IllegalArgumentException("Scenario not found: " + scenario, ex);
        }
    }
}
