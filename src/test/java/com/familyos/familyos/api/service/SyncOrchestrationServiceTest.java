package com.familyos.familyos.api.service;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.domain.dto.DomainActionDto;
import com.familyos.familyos.domain.dto.DomainProcessResponse;
import com.familyos.familyos.domain.dto.SourceDocumentDto;
import com.familyos.familyos.domain.entity.ProcessingStatus;
import com.familyos.familyos.domain.entity.SourceDocument;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import com.familyos.familyos.domain.service.DomainPersistenceService;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.service.CalendarService;
import com.familyos.familyos.service.DriveService;
import com.familyos.familyos.service.GmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncOrchestrationServiceTest {

    @Mock private GmailService gmailService;
    @Mock private CalendarService calendarService;
    @Mock private DriveService driveService;
    @Mock private DomainPersistenceService domainPersistenceService;
    @Mock private SourceDocumentRepository sourceDocumentRepository;
    @Mock private AuthenticationService authenticationService;

    private SyncOrchestrationService syncOrchestrationService;

    @BeforeEach
    void setUp() {
        syncOrchestrationService = new SyncOrchestrationService(
            gmailService,
            calendarService,
            driveService,
            domainPersistenceService,
            sourceDocumentRepository,
            authenticationService
        );
    }

    @Test
    void syncAllImportsProcessesAndBuildsSummary() {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("u-1", "u@example.com", "User", "google"));
        when(gmailService.readLatestMessages("u-1")).thenReturn(List.of(
            new GmailMessageDto("msg-1", "t1", "a@b.com", "Subject 1", "now", "Snippet 1"),
            new GmailMessageDto("msg-2", "t2", "a@b.com", "Subject 2", "now", "Snippet 2")
        ));
        when(calendarService.readUpcomingEvents("u-1")).thenReturn(List.of());
        when(driveService.readRecentFiles("u-1")).thenReturn(List.of());

        when(sourceDocumentRepository.findByProviderAndExternalIdAndSourceType(anyString(), anyString(), anyString()))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(new SourceDocument()));

        SourceDocument newDoc = new SourceDocument();
        newDoc.setId(UUID.randomUUID());
        newDoc.setSourceType("gmail");
        newDoc.setProcessingStatus(ProcessingStatus.NEW);
        when(sourceDocumentRepository.findAllByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus.NEW))
            .thenReturn(List.of(newDoc));

        DomainProcessResponse processResponse = new DomainProcessResponse(
            new SourceDocumentDto(newDoc.getId(), "msg-1", "google", "gmail", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "PROCESSED"),
            null,
            List.of(
                new DomainActionDto(UUID.randomUUID(), "TASK", "Task", "Desc", "OPEN", 0.9),
                new DomainActionDto(UUID.randomUUID(), "EVENT", "Event", "Desc", "OPEN", 0.8),
                new DomainActionDto(UUID.randomUUID(), "REMINDER", "Reminder", "Desc", "OPEN", 0.7)
            )
        );
        when(domainPersistenceService.processPersistedSourceDocument(newDoc.getId(), false)).thenReturn(processResponse);

        var response = syncOrchestrationService.syncAll();

        assertEquals("COMPLETED", response.status());
        assertEquals(2, response.documentsRead());
        assertEquals(1, response.documentsImported());
        assertEquals(1, response.documentsSkipped());
        assertEquals(1, response.documentsProcessed());
        assertEquals(1, response.tasksCreated());
        assertEquals(1, response.eventsCreated());
        assertEquals(1, response.remindersCreated());
        verify(domainPersistenceService, times(1)).persistSourceDocument(any());
    }

    @Test
    void syncGmailOnlyProcessesGmailNewDocuments() {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("u-1", "u@example.com", "User", "google"));
        when(gmailService.readLatestMessages("u-1")).thenReturn(List.of());

        SourceDocument gmailDoc = new SourceDocument();
        gmailDoc.setId(UUID.randomUUID());
        gmailDoc.setSourceType("gmail");
        gmailDoc.setProcessingStatus(ProcessingStatus.NEW);

        SourceDocument calendarDoc = new SourceDocument();
        calendarDoc.setId(UUID.randomUUID());
        calendarDoc.setSourceType("calendar");
        calendarDoc.setProcessingStatus(ProcessingStatus.NEW);

        when(sourceDocumentRepository.findAllByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus.NEW))
            .thenReturn(List.of(gmailDoc, calendarDoc));

        when(domainPersistenceService.processPersistedSourceDocument(gmailDoc.getId(), false))
            .thenReturn(new DomainProcessResponse(
                new SourceDocumentDto(gmailDoc.getId(), "g1", "google", "gmail", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "PROCESSED"),
                null,
                List.of()
            ));

        syncOrchestrationService.syncGmail();

        verify(domainPersistenceService).processPersistedSourceDocument(gmailDoc.getId(), false);
        verify(domainPersistenceService, never()).processPersistedSourceDocument(calendarDoc.getId(), false);
    }
}
