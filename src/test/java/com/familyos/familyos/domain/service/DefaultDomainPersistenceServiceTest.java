package com.familyos.familyos.domain.service;

import com.familyos.familyos.domain.dto.DomainProcessResponse;
import com.familyos.familyos.domain.dto.ProcessExtractionRequest;
import com.familyos.familyos.domain.entity.Action;
import com.familyos.familyos.domain.entity.Extraction;
import com.familyos.familyos.domain.entity.ProcessingStatus;
import com.familyos.familyos.domain.entity.SourceDocument;
import com.familyos.familyos.domain.entity.Task;
import com.familyos.familyos.domain.exception.DomainPersistenceException;
import com.familyos.familyos.domain.exception.DomainValidationException;
import com.familyos.familyos.domain.mapper.ActionMapper;
import com.familyos.familyos.domain.repository.ActionRepository;
import com.familyos.familyos.domain.repository.EventRepository;
import com.familyos.familyos.domain.repository.ExtractionRepository;
import com.familyos.familyos.domain.repository.ReminderRepository;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import com.familyos.familyos.domain.repository.TaskRepository;
import com.familyos.familyos.domain.validation.ProcessExtractionRequestValidator;
import com.familyos.familyos.extraction.dto.ActionCandidate;
import com.familyos.familyos.extraction.dto.ExtractionResponse;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.extraction.service.ExtractionService;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultDomainPersistenceServiceTest {

    @Mock private SourceDocumentRepository sourceDocumentRepository;
    @Mock private ExtractionRepository extractionRepository;
    @Mock private ActionRepository actionRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ReminderRepository reminderRepository;
    @Mock private ActionMapper actionMapper;
    @Mock private ExtractionService extractionService;

    private DefaultDomainPersistenceService service;

    @BeforeEach
    void setUp() {
        service = new DefaultDomainPersistenceService(
            new ProcessExtractionRequestValidator(),
            sourceDocumentRepository,
            extractionRepository,
            actionRepository,
            taskRepository,
            eventRepository,
            reminderRepository,
            actionMapper,
            extractionService
        );

        lenient().when(sourceDocumentRepository.save(any(SourceDocument.class))).thenAnswer(invocation -> {
            SourceDocument sourceDocument = invocation.getArgument(0);
            if (sourceDocument.getId() == null) {
                sourceDocument.setId(UUID.randomUUID());
            }
            return sourceDocument;
        });
        lenient().when(extractionRepository.save(any(Extraction.class))).thenAnswer(invocation -> {
            Extraction extraction = invocation.getArgument(0);
            if (extraction.getId() == null) {
                extraction.setId(UUID.randomUUID());
            }
            return extraction;
        });
    }

    @Test
    void persistsNewDocumentAndActions() {
        ProcessExtractionRequest request = validRequestWithExtraction();
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Confirm attendance");
        task.setStatus("OPEN");
        task.setConfidence(0.91);

        when(sourceDocumentRepository.findByProviderAndExternalIdAndSourceType("google", "msg-1", "gmail"))
            .thenReturn(Optional.empty());
        when(sourceDocumentRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            SourceDocument sourceDocument = new SourceDocument();
            sourceDocument.setId(id);
            sourceDocument.setProvider("google");
            sourceDocument.setSourceType("gmail");
            sourceDocument.setExternalId("msg-1");
            sourceDocument.setReceivedAt(LocalDateTime.now());
            sourceDocument.setRawContent("Meeting next Friday");
            sourceDocument.setMetadata(Map.of("model", "gemini-2.5-flash", "llmProvider", "google", "promptVersion", "v1"));
            sourceDocument.setProcessingStatus(ProcessingStatus.NEW);
            return Optional.of(sourceDocument);
        });
        when(extractionRepository.findBySourceDocumentId(any(UUID.class))).thenReturn(Optional.empty());
        when(actionMapper.mapToEntities(any(), any(), any())).thenReturn(List.of(task));
        when(actionRepository.saveAll(any())).thenReturn(List.of(task));

        DomainProcessResponse response = service.process(request);

        assertNotNull(response);
        assertEquals("msg-1", response.sourceDocument().externalId());
        assertEquals("PROCESSED", response.sourceDocument().processingStatus());
        assertEquals(1, response.actions().size());
        verify(extractionService, never()).process(any());
    }

    @Test
    void skipsAutomaticProcessingForAlreadyProcessedDocument() {
        ProcessExtractionRequest request = validRequestWithoutExtraction();
        SourceDocument existing = new SourceDocument();
        existing.setId(UUID.randomUUID());
        existing.setExternalId("msg-1");
        existing.setProvider("google");
        existing.setSourceType("gmail");
        existing.setReceivedAt(LocalDateTime.now());
        existing.setProcessingStatus(ProcessingStatus.PROCESSED);

        when(sourceDocumentRepository.findByProviderAndExternalIdAndSourceType("google", "msg-1", "gmail"))
            .thenReturn(Optional.of(existing));
        when(sourceDocumentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(extractionRepository.findBySourceDocumentId(existing.getId())).thenReturn(Optional.empty());
        when(actionRepository.findAllBySourceDocumentId(existing.getId())).thenReturn(List.of());

        DomainProcessResponse response = service.process(request);

        assertEquals("PROCESSED", response.sourceDocument().processingStatus());
        verify(extractionService, never()).process(any());
        verify(actionRepository, never()).deleteBySourceDocumentId(any(UUID.class));
    }

    @Test
    void marksSkippedWhenRuleEngineSkips() {
        ProcessExtractionRequest request = validRequestWithoutExtraction();
        SourceDocument persisted = new SourceDocument();
        persisted.setId(UUID.randomUUID());
        persisted.setExternalId("msg-1");
        persisted.setProvider("google");
        persisted.setSourceType("gmail");
        persisted.setReceivedAt(LocalDateTime.now());
        persisted.setProcessingStatus(ProcessingStatus.NEW);

        when(sourceDocumentRepository.findByProviderAndExternalIdAndSourceType("google", "msg-1", "gmail"))
            .thenReturn(Optional.of(persisted));
        when(sourceDocumentRepository.findById(persisted.getId())).thenReturn(Optional.of(persisted));
        when(extractionRepository.findBySourceDocumentId(persisted.getId())).thenReturn(Optional.empty());
        when(actionRepository.findAllBySourceDocumentId(persisted.getId())).thenReturn(List.of());
        when(extractionService.process(any())).thenReturn(
            ExtractionResponse.skipped(new RuleResult(RuleDecision.IGNORE, "LabelRule", "spam", 10))
        );

        DomainProcessResponse response = service.process(request);

        assertEquals("SKIPPED", response.sourceDocument().processingStatus());
    }

    @Test
    void marksFailedWhenExtractionFails() {
        ProcessExtractionRequest request = validRequestWithoutExtraction();
        SourceDocument persisted = new SourceDocument();
        persisted.setId(UUID.randomUUID());
        persisted.setExternalId("msg-1");
        persisted.setProvider("google");
        persisted.setSourceType("gmail");
        persisted.setReceivedAt(LocalDateTime.now());
        persisted.setProcessingStatus(ProcessingStatus.NEW);

        when(sourceDocumentRepository.findByProviderAndExternalIdAndSourceType("google", "msg-1", "gmail"))
            .thenReturn(Optional.of(persisted));
        when(sourceDocumentRepository.findById(persisted.getId())).thenReturn(Optional.of(persisted));
        when(extractionService.process(any())).thenReturn(ExtractionResponse.error("Provider timeout"));

        assertThrows(DomainPersistenceException.class, () -> service.process(request));
    }

    @Test
    void rejectsInvalidRequest() {
        NormalizedDocument source = new NormalizedDocument(
            "id", "sender", "subject", "content", List.of(), "1", "email",
            null, "gmail", "msg-1", "content", Map.of()
        );
        ProcessExtractionRequest invalid = new ProcessExtractionRequest(source, new ExtractionResult("summary", 0.9, List.of()));

        assertThrows(DomainValidationException.class, () -> service.process(invalid));
        verify(sourceDocumentRepository, never()).save(any());
    }

    private ProcessExtractionRequest validRequestWithExtraction() {
        NormalizedDocument source = new NormalizedDocument(
            "doc-1",
            "school@example.com",
            "Meeting reminder",
            "Meeting next Friday",
            List.of("school"),
            "1",
            "email",
            "google",
            "gmail",
            "msg-1",
            "Meeting next Friday",
            Map.of("model", "gemini-2.5-flash", "llmProvider", "google", "promptVersion", "v1")
        );
        ExtractionResult result = new ExtractionResult(
            "summary",
            0.91,
            List.of(new ActionCandidate("TASK", "Confirm attendance", "with teacher", null))
        );
        return new ProcessExtractionRequest(source, result);
    }

    private ProcessExtractionRequest validRequestWithoutExtraction() {
        NormalizedDocument source = new NormalizedDocument(
            "doc-1",
            "school@example.com",
            "Meeting reminder",
            "Meeting next Friday",
            List.of("school"),
            "1",
            "email",
            "google",
            "gmail",
            "msg-1",
            "Meeting next Friday",
            Map.of("model", "gemini-2.5-flash", "llmProvider", "google", "promptVersion", "v1")
        );
        return new ProcessExtractionRequest(source, null);
    }
}
