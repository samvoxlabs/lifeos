package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.SyncSummaryResponse;
import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.domain.dto.DomainProcessResponse;
import com.familyos.familyos.domain.dto.SourceDocumentDto;
import com.familyos.familyos.domain.entity.ProcessingStatus;
import com.familyos.familyos.domain.entity.SourceDocument;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import com.familyos.familyos.domain.service.DomainPersistenceService;
import com.familyos.familyos.dto.CalendarEventDto;
import com.familyos.familyos.dto.DriveFileDto;
import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.service.CalendarService;
import com.familyos.familyos.service.DriveService;
import com.familyos.familyos.service.GmailService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SyncOrchestrationService {

    private static final String PROVIDER = "google";
    private static final String SOURCE_GMAIL = "gmail";
    private static final String SOURCE_CALENDAR = "calendar";
    private static final String SOURCE_DRIVE = "drive";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_PROCESSED = "PROCESSED";

    private final GmailService gmailService;
    private final CalendarService calendarService;
    private final DriveService driveService;
    private final DomainPersistenceService domainPersistenceService;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final AuthenticationService authenticationService;

    public SyncOrchestrationService(
        GmailService gmailService,
        CalendarService calendarService,
        DriveService driveService,
        DomainPersistenceService domainPersistenceService,
        SourceDocumentRepository sourceDocumentRepository,
        AuthenticationService authenticationService
    ) {
        this.gmailService = gmailService;
        this.calendarService = calendarService;
        this.driveService = driveService;
        this.domainPersistenceService = domainPersistenceService;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.authenticationService = authenticationService;
    }

    public SyncSummaryResponse syncAll() {
        return syncSources(Set.of(SOURCE_GMAIL, SOURCE_CALENDAR, SOURCE_DRIVE));
    }

    public SyncSummaryResponse syncGmail() {
        return syncSources(Set.of(SOURCE_GMAIL));
    }

    public SyncSummaryResponse syncCalendar() {
        return syncSources(Set.of(SOURCE_CALENDAR));
    }

    public SyncSummaryResponse syncDrive() {
        return syncSources(Set.of(SOURCE_DRIVE));
    }

    public SyncSummaryResponse processPendingDocuments() {
        long start = System.currentTimeMillis();
        SyncCounters counters = new SyncCounters();

        processNewDocuments(Set.of(SOURCE_GMAIL, SOURCE_CALENDAR, SOURCE_DRIVE), counters);

        long duration = System.currentTimeMillis() - start;
        return new SyncSummaryResponse(
            STATUS_COMPLETED,
            0,
            0,
            0,
            counters.documentsProcessed,
            counters.tasksCreated,
            counters.eventsCreated,
            counters.remindersCreated,
            duration
        );
    }

    private SyncSummaryResponse syncSources(Set<String> sources) {
        long start = System.currentTimeMillis();
        String userId = authenticationService.currentUser().id();

        SyncCounters counters = new SyncCounters();

        if (sources.contains(SOURCE_GMAIL)) {
            List<NormalizedDocument> gmailDocuments = gmailService.readLatestMessages(userId).stream()
                .map(this::toGmailNormalizedDocument)
                .toList();
            importDocuments(gmailDocuments, counters);
        }
        if (sources.contains(SOURCE_CALENDAR)) {
            List<NormalizedDocument> calendarDocuments = calendarService.readUpcomingEvents(userId).stream()
                .map(this::toCalendarNormalizedDocument)
                .toList();
            importDocuments(calendarDocuments, counters);
        }
        if (sources.contains(SOURCE_DRIVE)) {
            List<NormalizedDocument> driveDocuments = driveService.readRecentFiles(userId).stream()
                .map(this::toDriveNormalizedDocument)
                .toList();
            importDocuments(driveDocuments, counters);
        }

        processNewDocuments(sources, counters);

        long duration = System.currentTimeMillis() - start;
        return new SyncSummaryResponse(
            STATUS_COMPLETED,
            counters.documentsRead,
            counters.documentsImported,
            counters.documentsSkipped,
            counters.documentsProcessed,
            counters.tasksCreated,
            counters.eventsCreated,
            counters.remindersCreated,
            duration
        );
    }

    private void importDocuments(List<NormalizedDocument> documents, SyncCounters counters) {
        for (NormalizedDocument document : documents) {
            counters.documentsRead++;

            if (document.externalId() == null || document.externalId().isBlank()) {
                counters.documentsSkipped++;
                continue;
            }

            Optional<SourceDocument> existing = sourceDocumentRepository.findByProviderAndExternalIdAndSourceType(
                document.provider(),
                document.externalId(),
                document.sourceType()
            );
            if (existing.isPresent()) {
                counters.documentsSkipped++;
                continue;
            }

            domainPersistenceService.persistSourceDocument(document);
            counters.documentsImported++;
        }
    }

    private void processNewDocuments(Set<String> sources, SyncCounters counters) {
        List<SourceDocument> newDocuments = sourceDocumentRepository.findAllByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus.NEW);
        Set<String> sourceFilter = new HashSet<>(sources);

        for (SourceDocument sourceDocument : newDocuments) {
            if (!sourceFilter.contains(sourceDocument.getSourceType())) {
                continue;
            }

            DomainProcessResponse response = domainPersistenceService.processPersistedSourceDocument(sourceDocument.getId(), false);
            SourceDocumentDto sourceDocumentDto = response.sourceDocument();
            if (sourceDocumentDto != null && STATUS_PROCESSED.equalsIgnoreCase(sourceDocumentDto.processingStatus())) {
                counters.documentsProcessed++;
            }

            response.actions().forEach(action -> {
                String type = action.type();
                if ("TASK".equalsIgnoreCase(type)) {
                    counters.tasksCreated++;
                } else if ("EVENT".equalsIgnoreCase(type)) {
                    counters.eventsCreated++;
                } else if ("REMINDER".equalsIgnoreCase(type)) {
                    counters.remindersCreated++;
                }
            });
        }
    }

    private NormalizedDocument toGmailNormalizedDocument(GmailMessageDto message) {
        String body = fallback(message.snippet(), message.subject(), "gmail-message");
        return new NormalizedDocument(
            message.id(),
            fallback(message.from(), "google-gmail"),
            fallback(message.subject(), "Gmail message"),
            body,
            List.of(),
            "normal",
            SOURCE_GMAIL,
            PROVIDER,
            SOURCE_GMAIL,
            fallback(message.id(), "gmail-" + UUID.randomUUID()),
            body,
            Map.of(
                "threadId", fallback(message.threadId(), ""),
                "messageDate", fallback(message.date(), "")
            )
        );
    }

    private NormalizedDocument toCalendarNormalizedDocument(CalendarEventDto event) {
        String content = String.join(" | ",
            fallback(event.description(), "calendar-event"),
            fallback(event.location(), "no-location"),
            fallback(event.start(), "no-start"),
            fallback(event.end(), "no-end")
        );
        return new NormalizedDocument(
            event.id(),
            "google-calendar",
            fallback(event.summary(), "Calendar event"),
            content,
            List.of(),
            "normal",
            SOURCE_CALENDAR,
            PROVIDER,
            SOURCE_CALENDAR,
            fallback(event.id(), "calendar-" + UUID.randomUUID()),
            content,
            Map.of(
                "status", fallback(event.status(), ""),
                "start", fallback(event.start(), ""),
                "end", fallback(event.end(), "")
            )
        );
    }

    private NormalizedDocument toDriveNormalizedDocument(DriveFileDto file) {
        String content = String.join(" | ",
            fallback(file.name(), "drive-file"),
            fallback(file.mimeType(), "unknown"),
            fallback(file.webViewLink(), "")
        );
        return new NormalizedDocument(
            file.id(),
            "google-drive",
            fallback(file.name(), "Drive file"),
            content,
            List.of(),
            "normal",
            SOURCE_DRIVE,
            PROVIDER,
            SOURCE_DRIVE,
            fallback(file.id(), "drive-" + UUID.randomUUID()),
            content,
            Map.of(
                "mimeType", fallback(file.mimeType(), ""),
                "modifiedTime", fallback(file.modifiedTime(), ""),
                "size", fallback(file.size(), "")
            )
        );
    }

    private String fallback(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private String fallback(String value, String firstDefault, String secondDefault) {
        if (value == null || value.isBlank()) {
            return fallback(firstDefault, secondDefault);
        }
        return value;
    }

    private static final class SyncCounters {
        int documentsRead;
        int documentsImported;
        int documentsSkipped;
        int documentsProcessed;
        int tasksCreated;
        int eventsCreated;
        int remindersCreated;
    }
}
