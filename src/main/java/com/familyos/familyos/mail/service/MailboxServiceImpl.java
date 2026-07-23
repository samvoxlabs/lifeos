package com.familyos.familyos.mail.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailMailboxAdapter;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailMailboxSyncResult;
import com.familyos.familyos.integrations.google.gmail.GoogleGmailNormalizedMessage;
import com.familyos.familyos.mail.api.dto.MailConflictOptionResponse;
import com.familyos.familyos.mail.api.dto.MailConflictResolveResponse;
import com.familyos.familyos.mail.api.dto.MailConflictResponse;
import com.familyos.familyos.mail.api.dto.MailEventResponse;
import com.familyos.familyos.mail.api.dto.MailEventSourceResponse;
import com.familyos.familyos.mail.api.dto.MailMessagePatchRequest;
import com.familyos.familyos.mail.api.dto.MailMessageResponse;
import com.familyos.familyos.mail.api.dto.MailMessagesPageResponse;
import com.familyos.familyos.mail.api.dto.MailNotificationResponse;
import com.familyos.familyos.mail.api.dto.MailParticipantResponse;
import com.familyos.familyos.mail.api.dto.MailResolutionRequest;
import com.familyos.familyos.mail.api.dto.MailSyncRequest;
import com.familyos.familyos.mail.api.dto.MailSyncResponse;
import com.familyos.familyos.mail.api.dto.ReviewScheduleRequest;
import com.familyos.familyos.mail.api.dto.ReviewScheduleResponse;
import com.familyos.familyos.mail.entity.MailConflict;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.entity.MailMessage;
import com.familyos.familyos.mail.entity.MailResolutionExecution;
import com.familyos.familyos.mail.entity.MailSyncState;
import com.familyos.familyos.mail.exception.MailApiException;
import com.familyos.familyos.mail.model.DetectedConflict;
import com.familyos.familyos.mail.model.ExtractedMailEventCandidate;
import com.familyos.familyos.mail.model.GoogleAccountContext;
import com.familyos.familyos.mail.repository.MailConflictRepository;
import com.familyos.familyos.mail.repository.MailExtractedEventRepository;
import com.familyos.familyos.mail.repository.MailMessageRepository;
import com.familyos.familyos.mail.repository.MailResolutionExecutionRepository;
import com.familyos.familyos.mail.repository.MailSyncStateRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class MailboxServiceImpl implements MailboxService {

    private static final Logger log = LoggerFactory.getLogger(MailboxServiceImpl.class);
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_OWNER_ID = "mom";
    private static final String DEFAULT_TIMEZONE = "America/Chicago";

    private final GoogleAccountContextResolver googleAccountContextResolver;
    private final GoogleGmailMailboxAdapter gmailMailboxAdapter;
    private final MailSyncStateRepository mailSyncStateRepository;
    private final MailMessageRepository mailMessageRepository;
    private final MailExtractedEventRepository mailExtractedEventRepository;
    private final MailConflictRepository mailConflictRepository;
    private final MailResolutionExecutionRepository mailResolutionExecutionRepository;
    private final MailEventExtractionService mailEventExtractionService;
    private final MailConflictDetectionService mailConflictDetectionService;
    private final MailNotificationService mailNotificationService;
    private final LlmMailEventExtractionService llmMailEventExtractionService;
    private final EmailRuleEngineService emailRuleEngineService;

    public MailboxServiceImpl(
            GoogleAccountContextResolver googleAccountContextResolver,
            GoogleGmailMailboxAdapter gmailMailboxAdapter,
            MailSyncStateRepository mailSyncStateRepository,
            MailMessageRepository mailMessageRepository,
            MailExtractedEventRepository mailExtractedEventRepository,
            MailConflictRepository mailConflictRepository,
            MailResolutionExecutionRepository mailResolutionExecutionRepository,
            MailEventExtractionService mailEventExtractionService,
            MailConflictDetectionService mailConflictDetectionService,
            MailNotificationService mailNotificationService,
            LlmMailEventExtractionService llmMailEventExtractionService,
            EmailRuleEngineService emailRuleEngineService
    ) {
        this.googleAccountContextResolver = googleAccountContextResolver;
        this.gmailMailboxAdapter = gmailMailboxAdapter;
        this.mailSyncStateRepository = mailSyncStateRepository;
        this.mailMessageRepository = mailMessageRepository;
        this.mailExtractedEventRepository = mailExtractedEventRepository;
        this.mailConflictRepository = mailConflictRepository;
        this.mailResolutionExecutionRepository = mailResolutionExecutionRepository;
        this.mailEventExtractionService = mailEventExtractionService;
        this.mailConflictDetectionService = mailConflictDetectionService;
        this.mailNotificationService = mailNotificationService;
        this.llmMailEventExtractionService = llmMailEventExtractionService;
        this.emailRuleEngineService = emailRuleEngineService;
    }

    @Override
    @Transactional
    public MailSyncResponse syncMailbox(String userId, MailSyncRequest request) {
        GoogleAccountContext context = googleAccountContextResolver.resolve(userId);
        OAuthAccount account = context.account();
        validateMailboxRequest(account, request == null ? null : request.mailbox());

        MailSyncState syncState = mailSyncStateRepository.findByAccount(account).orElseGet(() -> {
            MailSyncState state = new MailSyncState();
            state.setAccount(account);
            return state;
        });

        int limit = normalizeLimit(request == null ? null : request.maxResults());
        String cursor = request != null && request.cursor() != null && !request.cursor().isBlank()
                ? request.cursor()
                : syncState.getLastHistoryId();

        GoogleGmailMailboxSyncResult syncResult = gmailMailboxAdapter.fetchNewMessages(context.accessToken(), cursor, limit);

        List<MailMessageResponse> messages = new ArrayList<>();

        for (GoogleGmailNormalizedMessage incoming : syncResult.messages()) {
            if (mailMessageRepository.findByAccountAndGmailMessageId(account, incoming.gmailMessageId()).isPresent()) {
                continue;
            }
            MailMessage message = new MailMessage();
            message.setAccount(account);
            message.setGmailMessageId(incoming.gmailMessageId());
            message.setThreadId(incoming.threadId());
            message.setSenderName(incoming.senderName());
            message.setSenderEmail(incoming.senderEmail());
            message.setRecipients(incoming.to());
            message.setSubject(incoming.subject());
            message.setSnippet(incoming.snippet());
            message.setBodyText(incoming.bodyText());
            message.setReceivedAt(incoming.receivedAt());
            message.setHistoryId(incoming.historyId());
            message.setRead(incoming.read());
            message.setLabels(incoming.labels());
            message.setHasActionableEvent(false);
            message = mailMessageRepository.save(message);
            messages.add(toMessageResponse(message, null, List.of()));
        }

        if (messages.isEmpty() || syncResult.messages().isEmpty()) {
            List<MailMessage> existing = mailMessageRepository.findByAccountOrderByReceivedAtDesc(
                    account, PageRequest.of(0, limit));
            for (MailMessage msg : existing) {
                Optional<MailExtractedEvent> event = mailExtractedEventRepository.findByMessage(msg);
                List<MailConflict> msgConflicts = event
                        .map(mailConflictRepository::findByExtractedEvent)
                        .orElse(List.of());
                messages.add(toMessageResponse(msg, event.orElse(null), msgConflicts));
            }
        }

        OffsetDateTime syncedAt = OffsetDateTime.now();
        syncState.setLastHistoryId(syncResult.historyCursor());
        syncState.setLastSyncedAt(syncedAt);
        mailSyncStateRepository.save(syncState);

        return new MailSyncResponse(
                mailboxOf(account),
                syncedAt,
                syncResult.historyCursor(),
                messages,
                List.of(),
                List.of()
        );
    }

    @Override
    @Transactional
    public MailMessagesPageResponse listMessages(String userId, Integer limit, String cursor) {
        GoogleAccountContext context = googleAccountContextResolver.resolve(userId);
        int pageLimit = normalizeLimit(limit);
        PageRequest page = PageRequest.of(0, pageLimit);
        OffsetDateTime pageCursor = parseCursor(cursor);

        List<MailMessage> messages = (pageCursor == null)
                ? mailMessageRepository.findByAccountOrderByReceivedAtDesc(context.account(), page)
                : mailMessageRepository.findByAccountAndReceivedAtLessThanOrderByReceivedAtDesc(
                        context.account(), pageCursor, page
                );

        List<MailMessageResponse> items = messages.stream()
                .map(message -> toMessageResponse(
                        message,
                        mailExtractedEventRepository.findByMessage(message).orElse(null),
                        mailExtractedEventRepository.findByMessage(message).map(mailConflictRepository::findByExtractedEvent).orElse(List.of())
                ))
                .toList();

        String nextCursor = messages.size() < pageLimit ? null : messages.get(messages.size() - 1).getReceivedAt().toString();
        return new MailMessagesPageResponse(items, nextCursor);
    }

    @Override
    @Transactional
    public ReviewScheduleResponse reviewSchedule(String userId, ReviewScheduleRequest request) {
        GoogleAccountContext context = googleAccountContextResolver.resolve(userId);
        OAuthAccount account = context.account();

        int limit = request != null && request.maxMessages() != null ? request.maxMessages() : 50;
        List<MailMessage> allMessages = mailMessageRepository.findByAccountOrderByReceivedAtDesc(account, PageRequest.of(0, limit));

        List<MailMessage> relevantMessages = allMessages.stream()
                .filter(emailRuleEngineService::isRelevant)
                .toList();

        List<MailEventResponse> events = new ArrayList<>();
        List<MailConflictResponse> conflicts = new ArrayList<>();

        for (MailMessage message : relevantMessages) {
            Optional<MailExtractedEvent> existingEvent = mailExtractedEventRepository.findByMessage(message);
            if (existingEvent.isPresent() && "confirmed".equals(existingEvent.get().getStatus())) {
                events.add(toEventResponse(existingEvent.get()));
                continue;
            }

            Optional<ExtractedMailEventCandidate> extracted = llmMailEventExtractionService.extractFromMessage(message);
            message.setHasActionableEvent(extracted.isPresent());
            mailMessageRepository.save(message);

            if (extracted.isPresent()) {
                MailExtractedEvent event;
                if (existingEvent.isPresent()) {
                    event = existingEvent.get();
                } else {
                    event = createProposedEvent(message, extracted.get());
                }

                List<DetectedConflict> candidates = mailConflictDetectionService.detectConflicts(account, event, context.accessToken());
                List<MailConflict> detectedConflicts = candidates.stream()
                        .map(c -> upsertConflict(event, c))
                        .toList();

                event.setStatus(detectedConflicts.isEmpty() ? "ready_to_publish" : "conflict");
                mailExtractedEventRepository.save(event);

                events.add(toEventResponse(event));
                conflicts.addAll(detectedConflicts.stream().map(this::toConflictResponse).toList());
            }
        }

        return new ReviewScheduleResponse(
                mailboxOf(account),
                OffsetDateTime.now().toString(),
                allMessages.size(),
                relevantMessages.size(),
                events,
                conflicts
        );
    }

    @Override
    @Transactional
    public MailMessageResponse getMessage(String userId, String messageId) {
        GoogleAccountContext context = googleAccountContextResolver.resolve(userId);
        MailMessage message = findMessage(context.account(), messageId);
        MailExtractedEvent event = mailExtractedEventRepository.findByMessage(message).orElse(null);
        List<MailConflict> conflicts = event == null ? List.of() : mailConflictRepository.findByExtractedEvent(event);
        return toMessageResponse(message, event, conflicts);
    }

    @Override
    @Transactional
    public MailMessageResponse updateMessage(String userId, String messageId, MailMessagePatchRequest request) {
        GoogleAccountContext context = googleAccountContextResolver.resolve(userId);
        MailMessage message = findMessage(context.account(), messageId);
        message.setRead(Boolean.TRUE.equals(request.read()));
        message = mailMessageRepository.save(message);
        MailExtractedEvent event = mailExtractedEventRepository.findByMessage(message).orElse(null);
        List<MailConflict> conflicts = event == null ? List.of() : mailConflictRepository.findByExtractedEvent(event);
        return toMessageResponse(message, event, conflicts);
    }

    @Override
    @Transactional
    public MailConflictResolveResponse resolveConflict(String userId, String conflictId, MailResolutionRequest request, String idempotencyKey) {
        GoogleAccountContext context = googleAccountContextResolver.resolve(userId);
        UUID conflictUuid = parseConflictId(conflictId);
        MailConflict conflict = mailConflictRepository.findByIdAndExtractedEventMessageAccount(conflictUuid, context.account())
                .orElseThrow(() -> new MailApiException(HttpStatus.NOT_FOUND, "CONFLICT_NOT_FOUND", "Conflict not found", false));

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<MailResolutionExecution> existing = mailResolutionExecutionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return toResolveResponse(conflict, existing.get().getActionKey(), existing.get().getNotificationSentAt() != null, false);
            }
        }

        if ("resolved".equalsIgnoreCase(conflict.getStatus())) {
            throw new MailApiException(HttpStatus.CONFLICT, "CONFLICT_ALREADY_RESOLVED", "Conflict already resolved", false);
        }

        String optionId = request.optionId();
        if (!supportedOptionIds(conflict).contains(optionId)) {
            throw new MailApiException(HttpStatus.BAD_REQUEST, "INVALID_RESOLUTION_OPTION", "Invalid resolution option", false);
        }

        MailExtractedEvent event = conflict.getExtractedEvent();
        boolean notificationSent = false;
        List<MailNotificationResponse> notifications = new ArrayList<>();

        if ("assign-family-member".equals(optionId)) {
            String delegate = request.delegateToEmail() == null || request.delegateToEmail().isBlank()
                    ? "dad@family.local"
                    : request.delegateToEmail();
            event.setAssignedToEmail(delegate);
        } else if ("delay-pickup".equals(optionId)) {
            event.setStartsAt(event.getStartsAt().plusHours(1));
            event.setEndsAt(event.getEndsAt().plusHours(1));
            event.setTitle(event.getTitle() + " — delayed 1 hour");
            String recipient = request.notifyRecipientEmail() == null || request.notifyRecipientEmail().isBlank()
                    ? "child@family.local"
                    : request.notifyRecipientEmail();
            mailNotificationService.sendResolutionNotification(conflict, event, recipient, optionId);
            notificationSent = true;
            notifications.add(new MailNotificationResponse(
                    "notification-" + conflict.getId(),
                    "child",
                    "email",
                    "sent",
                    "Airport pickup has been moved to 4:50 PM."
            ));
        }

        event.setStatus("ready_to_publish");
        mailExtractedEventRepository.save(event);

        conflict.setStatus("resolved");
        conflict.setAppliedResolutionKey(optionId);
        conflict.setResolvedAt(OffsetDateTime.now());
        mailConflictRepository.save(conflict);

        MailResolutionExecution execution = new MailResolutionExecution();
        execution.setConflict(conflict);
        execution.setActionKey(optionId);
        execution.setIdempotencyKey(idempotencyKey);
        execution.setCalendarUpdatedAt(OffsetDateTime.now());
        execution.setNotificationSentAt(notificationSent ? OffsetDateTime.now() : null);
        mailResolutionExecutionRepository.save(execution);

        MailConflictResolveResponse response = toResolveResponse(conflict, optionId, notificationSent, false);
        if (!notifications.isEmpty()) {
            return new MailConflictResolveResponse(
                    response.conflictId(),
                    response.status(),
                    response.selectedOptionId(),
                    response.updatedEvents(),
                    notifications,
                    response.resolvedAt(),
                    false
            );
        }
        return response;
    }

    private MailMessage findMessage(OAuthAccount account, String messageId) {
        return mailMessageRepository.findByAccountAndGmailMessageId(account, messageId)
                .orElseThrow(() -> new MailApiException(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", "Message not found", false));
    }

    private MailExtractedEvent createEvent(MailMessage message, ExtractedMailEventCandidate extracted) {
        MailExtractedEvent event = new MailExtractedEvent();
        event.setMessage(message);
        event.setTitle(extracted.title());
        event.setStartsAt(extracted.startsAt());
        event.setEndsAt(extracted.endsAt());
        event.setTimezoneOffset(extracted.startsAt().getOffset().getId());
        event.setTimezone(DEFAULT_TIMEZONE);
        event.setLocation(extracted.location());
        event.setEventDescription(extracted.description());
        event.setOwnerId(DEFAULT_OWNER_ID);
        event.setConfidence(extracted.confidence());
        event.setStatus("confirmed");
        return mailExtractedEventRepository.save(event);
    }

    private MailExtractedEvent createProposedEvent(MailMessage message, ExtractedMailEventCandidate extracted) {
        MailExtractedEvent event = new MailExtractedEvent();
        event.setMessage(message);
        event.setTitle(extracted.title());
        event.setStartsAt(extracted.startsAt());
        event.setEndsAt(extracted.endsAt());
        event.setTimezoneOffset(extracted.startsAt().getOffset().getId());
        event.setTimezone(DEFAULT_TIMEZONE);
        event.setLocation(extracted.location());
        event.setEventDescription(extracted.description());
        event.setOwnerId(null);
        event.setConfidence(extracted.confidence());
        event.setStatus("proposed");
        return mailExtractedEventRepository.save(event);
    }

    private MailConflict upsertConflict(MailExtractedEvent event, DetectedConflict detected) {
        MailConflict conflict = mailConflictRepository.findByExtractedEventAndConflictingEventIdAndConflictingEventSource(
                event,
                detected.conflictingEventId(),
                detected.conflictingEventSource()
        ).orElseGet(MailConflict::new);
        conflict.setExtractedEvent(event);
        conflict.setConflictingEventId(detected.conflictingEventId());
        conflict.setConflictingEventSource(detected.conflictingEventSource());
        conflict.setOverlapStart(detected.overlapStart());
        conflict.setOverlapEnd(detected.overlapEnd());
        conflict.setStatus("open");
        conflict.setSuggestedResolutions(defaultConflictOptions(event));
        return mailConflictRepository.save(conflict);
    }

    private Map<String, Object> defaultConflictOptions(MailExtractedEvent event) {
        String eventId = externalEventId(event);

        Map<String, Object> assignEffects = new LinkedHashMap<>();
        assignEffects.put("eventId", eventId);
        assignEffects.put("assigneeId", "dad");
        assignEffects.put("notifyMemberIds", List.of("dad", "child"));

        List<Map<String, Object>> assignSteps = List.of(
                Map.of("service", "calendar", "action", "update_event", "detail", "Reassign event owner"),
                Map.of("service", "gmail", "action", "draft_email", "detail", "Draft notification to assignee"),
                Map.of("service", "notifications", "action", "send_push", "detail", "Push notification to family member")
        );
        Map<String, Object> assignAgenticPlan = Map.of(
                "summary", "Reassign airport pickup to an available family member and send notification.",
                "steps", assignSteps
        );

        Map<String, Object> delayEffects = new LinkedHashMap<>();
        delayEffects.put("eventId", eventId);
        delayEffects.put("newStart", event.getStartsAt().plusHours(1).toString());
        delayEffects.put("newEnd", event.getEndsAt().plusHours(1).toString());
        delayEffects.put("notifyMemberIds", List.of("child"));

        List<Map<String, Object>> delaySteps = List.of(
                Map.of("service", "calendar", "action", "update_event", "detail", "Update event start and end times"),
                Map.of("service", "gmail", "action", "draft_email", "detail", "Draft delay notification"),
                Map.of("service", "notifications", "action", "send_push", "detail", "Push notification to child")
        );
        Map<String, Object> delayAgenticPlan = Map.of(
                "summary", "Delay airport pickup by 1 hour and notify the arriving family member.",
                "steps", delaySteps
        );

        Map<String, Object> cancelEffects = Map.of(
                "eventId", eventId,
                "notifyMemberIds", List.of("child", "dad")
        );
        List<Map<String, Object>> cancelSteps = List.of(
                Map.of("service", "calendar", "action", "delete_event", "detail", "Remove pickup event"),
                Map.of("service", "notifications", "action", "send_push", "detail", "Notify all family members")
        );
        Map<String, Object> cancelAgenticPlan = Map.of(
                "summary", "Cancel airport pickup event and notify all affected family members.",
                "steps", cancelSteps
        );

        List<Map<String, Object>> options = new ArrayList<>(List.of(
                Map.of(
                        "id", "assign-family-member",
                        "label", "Ask another family member",
                        "description", "Assign airport pickup to an available family member.",
                        "score", 0.94,
                        "reason", "Medical appointment cannot be moved; delegating pickup preserves both commitments.",
                        "recommended", true,
                        "agenticPlan", assignAgenticPlan,
                        "effects", assignEffects
                ),
                Map.of(
                        "id", "delay-pickup",
                        "label", "Delay pickup 1 hour",
                        "description", "Move pickup to one hour later and notify the arriving family member.",
                        "score", 0.76,
                        "reason", "Delaying pickup is feasible but may inconvenience the arriving traveler.",
                        "recommended", false,
                        "agenticPlan", delayAgenticPlan,
                        "effects", delayEffects
                ),
                Map.of(
                        "id", "cancel-pickup",
                        "label", "Cancel airport pickup",
                        "description", "Cancel the airport pickup and arrange alternative transport.",
                        "score", 0.45,
                        "reason", "Cancellation is a last resort and may inconvenience the arriving traveler.",
                        "recommended", false,
                        "agenticPlan", cancelAgenticPlan,
                        "effects", cancelEffects
                )
        ));
        return Map.of("options", options);
    }

    @SuppressWarnings("unchecked")
    private List<MailConflictOptionResponse> conflictOptions(MailConflict conflict) {
        Object raw = conflict.getSuggestedResolutions() == null ? null : conflict.getSuggestedResolutions().get("options");
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<MailConflictOptionResponse> options = new ArrayList<>();
        for (Object candidate : list) {
            if (!(candidate instanceof Map<?, ?> map)) {
                continue;
            }
            options.add(new MailConflictOptionResponse(
                    stringValue(map.get("id")),
                    stringValue(map.get("label")),
                    stringValue(map.get("description")),
                    Boolean.parseBoolean(stringValue(map.get("recommended"))),
                    map.get("effects") instanceof Map<?, ?> effects ? (Map<String, Object>) effects : Map.of()
            ));
        }
        return options;
    }

    private List<String> supportedOptionIds(MailConflict conflict) {
        return conflictOptions(conflict).stream().map(MailConflictOptionResponse::id).toList();
    }

    private MailMessageResponse toMessageResponse(MailMessage message, MailExtractedEvent event, List<MailConflict> conflicts) {
        String status;
        if (event == null) {
            status = "no_actionable_event";
        } else if (!conflicts.isEmpty()) {
            status = "conflict_detected";
        } else {
            status = "event_extracted";
        }
        return new MailMessageResponse(
                message.getGmailMessageId(),
                message.getThreadId(),
                new MailParticipantResponse(message.getSenderName(), message.getSenderEmail()),
                safeList(message.getRecipients()),
                message.getSubject(),
                message.getSnippet(),
                blankOrSnippet(message.getBodyText(), message.getSnippet()),
                message.getReceivedAt(),
                message.isRead(),
                safeList(message.getLabels()),
                status,
                event == null ? null : externalEventId(event)
        );
    }

    private MailEventResponse toEventResponse(MailExtractedEvent event) {
        return new MailEventResponse(
                externalEventId(event),
                event.getTitle(),
                event.getStartsAt().toString(),
                event.getEndsAt().toString(),
                event.getTimezone() == null ? DEFAULT_TIMEZONE : event.getTimezone(),
                event.getLocation(),
                event.getEventDescription(),
                event.getOwnerId() == null ? DEFAULT_OWNER_ID : event.getOwnerId(),
                event.getStatus(),
                new MailEventSourceResponse(
                        "email",
                        event.getMessage().getGmailMessageId(),
                        event.getMessage().getSenderEmail()
                ),
                event.getConfidence() == null ? 0.95d : event.getConfidence()
        );
    }

    private MailConflictResponse toConflictResponse(MailConflict conflict) {
        return new MailConflictResponse(
                externalConflictId(conflict),
                "time_overlap",
                "Schedule clash detected",
                "The delayed airport pickup overlaps with a family or Google calendar event.",
                List.of(conflict.getConflictingEventId(), externalEventId(conflict.getExtractedEvent())),
                conflict.getStatus(),
                conflictOptions(conflict)
        );
    }

    private MailConflictResolveResponse toResolveResponse(MailConflict conflict, String optionId, boolean notificationSent, boolean calendarUpdated) {
        MailEventResponse updated = toEventResponse(conflict.getExtractedEvent());
        List<MailNotificationResponse> notifications = notificationSent
                ? List.of(new MailNotificationResponse(
                        "notification-" + conflict.getId(),
                        "child",
                        "email",
                        "sent",
                        "Airport pickup has been moved to 4:50 PM."
                ))
                : List.of();
        return new MailConflictResolveResponse(
                externalConflictId(conflict),
                conflict.getStatus(),
                optionId,
                List.of(updated),
                notifications,
                conflict.getResolvedAt() == null ? OffsetDateTime.now() : conflict.getResolvedAt(),
                calendarUpdated
        );
    }

    private UUID parseConflictId(String value) {
        String raw = value;
        if (value != null && value.startsWith("conflict-")) {
            raw = value.substring("conflict-".length());
        }
        try {
            return UUID.fromString(raw);
        } catch (Exception ex) {
            throw new MailApiException(HttpStatus.NOT_FOUND, "CONFLICT_NOT_FOUND", "Conflict not found", false);
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new MailApiException(HttpStatus.BAD_REQUEST, "MAIL_SYNC_FAILED", "limit/maxResults must be between 1 and 100", false);
        }
        return limit;
    }

    private OffsetDateTime parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(cursor);
        } catch (Exception ex) {
            return null;
        }
    }

    private void validateMailboxRequest(OAuthAccount account, String mailbox) {
        if (mailbox == null || mailbox.isBlank()) {
            return;
        }
        String accountMailbox = mailboxOf(account);
        if (!mailbox.equalsIgnoreCase(accountMailbox)) {
            throw new MailApiException(HttpStatus.BAD_REQUEST, "GMAIL_NOT_CONNECTED", "Requested mailbox does not match authenticated Google account", false);
        }
    }

    private String mailboxOf(OAuthAccount account) {
        if (account.getEmail() != null && !account.getEmail().isBlank()) {
            return account.getEmail();
        }
        return "parentosfamily@gmail.com";
    }

    private String extractLocation(String bodyText) {
        if (bodyText == null || bodyText.isBlank()) {
            return null;
        }
        String lower = bodyText.toLowerCase();
        int at = lower.indexOf(" at ");
        if (at >= 0) {
            int end = bodyText.indexOf('.', at);
            return end > at ? bodyText.substring(at + 4, end).trim() : bodyText.substring(at + 4).trim();
        }
        return null;
    }

    private String externalEventId(MailExtractedEvent event) {
        return "event-" + event.getId();
    }

    private String externalConflictId(MailConflict conflict) {
        return "conflict-" + conflict.getId();
    }

    private List<String> safeList(List<String> value) {
        return value == null ? List.of() : value;
    }

    private String blankOrSnippet(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
