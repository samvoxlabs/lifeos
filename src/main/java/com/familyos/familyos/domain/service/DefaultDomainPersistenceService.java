package com.familyos.familyos.domain.service;

import com.familyos.familyos.domain.dto.DomainActionDto;
import com.familyos.familyos.domain.dto.DomainProcessResponse;
import com.familyos.familyos.domain.dto.ExtractionDto;
import com.familyos.familyos.domain.dto.ProcessExtractionRequest;
import com.familyos.familyos.domain.dto.SourceDocumentDto;
import com.familyos.familyos.domain.entity.Action;
import com.familyos.familyos.domain.entity.Extraction;
import com.familyos.familyos.domain.entity.ProcessingStatus;
import com.familyos.familyos.domain.entity.SourceDocument;
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
import com.familyos.familyos.extraction.dto.ExtractionResponse;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.extraction.service.ExtractionService;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DefaultDomainPersistenceService implements DomainPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(DefaultDomainPersistenceService.class);

    private final ProcessExtractionRequestValidator validator;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final ExtractionRepository extractionRepository;
    private final ActionRepository actionRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final ReminderRepository reminderRepository;
    private final ActionMapper actionMapper;
    private final ExtractionService extractionService;

    public DefaultDomainPersistenceService(
        ProcessExtractionRequestValidator validator,
        SourceDocumentRepository sourceDocumentRepository,
        ExtractionRepository extractionRepository,
        ActionRepository actionRepository,
        TaskRepository taskRepository,
        EventRepository eventRepository,
        ReminderRepository reminderRepository,
        ActionMapper actionMapper,
        ExtractionService extractionService
    ) {
        this.validator = validator;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.extractionRepository = extractionRepository;
        this.actionRepository = actionRepository;
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.reminderRepository = reminderRepository;
        this.actionMapper = actionMapper;
        this.extractionService = extractionService;
    }

    @Override
    @Transactional
    public DomainProcessResponse process(ProcessExtractionRequest request) {
        validator.validate(request);
        SourceDocument sourceDocument = persistSourceDocumentInternal(request.sourceDocument());
        return processPersistedSourceDocumentInternal(sourceDocument.getId(), request.extractionResult(), false);
    }

    @Override
    @Transactional
    public DomainProcessResponse processPersistedSourceDocument(UUID sourceDocumentId, boolean forceReprocess) {
        return processPersistedSourceDocumentInternal(sourceDocumentId, null, forceReprocess);
    }

    @Override
    @Transactional
    public void persistSourceDocument(NormalizedDocument sourceDocument) {
        persistSourceDocumentInternal(sourceDocument);
    }

    @Transactional
    DomainProcessResponse processPersistedSourceDocumentInternal(
        UUID sourceDocumentId,
        ExtractionResult overrideExtractionResult,
        boolean forceReprocess
    ) {
        SourceDocument sourceDocument = sourceDocumentRepository.findById(sourceDocumentId)
            .orElseThrow(() -> new DomainValidationException("sourceDocument not found: " + sourceDocumentId));

        if (!forceReprocess
            && overrideExtractionResult == null
            && sourceDocument.getProcessingStatus() != ProcessingStatus.NEW) {
            log.info("Skipping automatic processing for source document {} with status {}",
                sourceDocumentId, sourceDocument.getProcessingStatus());
            return buildResponseSnapshot(sourceDocument);
        }

        try {
            sourceDocument = updateStatus(sourceDocument, ProcessingStatus.PROCESSING);
            ExtractionResult extractionResult = overrideExtractionResult != null
                ? overrideExtractionResult
                : runExtraction(sourceDocument);

            if (extractionResult == null) {
                sourceDocument = updateStatus(sourceDocument, ProcessingStatus.SKIPPED);
                return buildResponseSnapshot(sourceDocument);
            }

            Extraction extraction = upsertExtraction(sourceDocument, extractionResult);
            actionRepository.deleteBySourceDocumentId(sourceDocument.getId());
            List<Action> mappedActions = actionMapper.mapToEntities(extractionResult, sourceDocument, extraction);
            List<Action> persistedActions = actionRepository.saveAll(mappedActions);
            sourceDocument = updateStatus(sourceDocument, ProcessingStatus.PROCESSED);

            return new DomainProcessResponse(
                toSourceDocumentDto(sourceDocument),
                toExtractionDto(extraction),
                persistedActions.stream().map(this::toActionDto).toList()
            );
        } catch (RuntimeException ex) {
            sourceDocumentRepository.findById(sourceDocumentId)
                .ifPresent(doc -> updateStatus(doc, ProcessingStatus.FAILED));
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainActionDto> getTasks() {
        return taskRepository.findAll().stream().map(this::toActionDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainActionDto> getEvents() {
        return eventRepository.findAll().stream().map(this::toActionDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainActionDto> getReminders() {
        return reminderRepository.findAll().stream().map(this::toActionDto).toList();
    }

    private SourceDocument persistSourceDocumentInternal(NormalizedDocument src) {
        String externalId = src.externalId();
        String rawContent = src.rawContent() != null ? src.rawContent() : src.content();
        Map<String, Object> metadata = src.metadata() != null ? src.metadata() : Map.of();

        SourceDocument sourceDocument = sourceDocumentRepository
            .findByProviderAndExternalIdAndSourceType(src.provider(), externalId, src.sourceType())
            .orElse(null);

        if (sourceDocument != null) {
            return sourceDocument;
        }

        sourceDocument = new SourceDocument();
        sourceDocument.setProvider(src.provider());
        sourceDocument.setExternalId(externalId);
        sourceDocument.setSourceType(src.sourceType());
        sourceDocument.setSender(src.sender());
        sourceDocument.setSubject(src.subject());
        sourceDocument.setRawContent(rawContent == null ? "" : rawContent);
        sourceDocument.setMetadata(metadata);
        sourceDocument.setReceivedAt(LocalDateTime.now());
        sourceDocument.setProcessingStatus(ProcessingStatus.NEW);
        log.info("Persisted new source document {} / {} / {}",
            sourceDocument.getProvider(), sourceDocument.getSourceType(), sourceDocument.getExternalId());
        return sourceDocumentRepository.save(sourceDocument);
    }

    private Extraction upsertExtraction(SourceDocument sourceDocument, ExtractionResult extractionResult) {
        Map<String, Object> metadata = sourceDocument.getMetadata() != null
            ? sourceDocument.getMetadata()
            : Map.of();

        Extraction extraction = extractionRepository.findBySourceDocumentId(sourceDocument.getId())
            .orElseGet(Extraction::new);

        extraction.setSourceDocument(sourceDocument);
        extraction.setSummary(extractionResult.summary());
        extraction.setConfidence(extractionResult.confidence());
        extraction.setModel(readMetadata(metadata, "model", "unknown-model"));
        extraction.setProvider(readMetadata(metadata, "llmProvider", sourceDocument.getProvider()));
        extraction.setPromptVersion(readMetadata(metadata, "promptVersion", "v1"));
        return extractionRepository.save(extraction);
    }

    private ExtractionResult runExtraction(SourceDocument sourceDocument) {
        ExtractionResponse extractionResponse = extractionService.process(toNormalizedDocument(sourceDocument));
        if ("SUCCESS".equals(extractionResponse.status())) {
            return extractionResponse.result();
        }
        if ("SKIPPED".equals(extractionResponse.status())) {
            return null;
        }
        String message = extractionResponse.message() == null
            ? "Extraction failed for source document " + sourceDocument.getId()
            : extractionResponse.message();
        throw new DomainPersistenceException(message);
    }

    private SourceDocument updateStatus(SourceDocument sourceDocument, ProcessingStatus status) {
        sourceDocument.setProcessingStatus(status);
        return sourceDocumentRepository.save(sourceDocument);
    }

    private DomainProcessResponse buildResponseSnapshot(SourceDocument sourceDocument) {
        ExtractionDto extractionDto = extractionRepository.findBySourceDocumentId(sourceDocument.getId())
            .map(this::toExtractionDto)
            .orElse(null);
        List<DomainActionDto> actions = actionRepository.findAllBySourceDocumentId(sourceDocument.getId())
            .stream()
            .map(this::toActionDto)
            .toList();
        return new DomainProcessResponse(toSourceDocumentDto(sourceDocument), extractionDto, actions);
    }

    private NormalizedDocument toNormalizedDocument(SourceDocument sourceDocument) {
        return new NormalizedDocument(
            sourceDocument.getId().toString(),
            sourceDocument.getSender(),
            sourceDocument.getSubject(),
            sourceDocument.getRawContent(),
            List.of(),
            "normal",
            sourceDocument.getSourceType(),
            sourceDocument.getProvider(),
            sourceDocument.getSourceType(),
            sourceDocument.getExternalId(),
            sourceDocument.getRawContent(),
            sourceDocument.getMetadata() == null ? Map.of() : sourceDocument.getMetadata()
        );
    }

    private String readMetadata(Map<String, Object> metadata, String key, String fallback) {
        Object value = metadata.get(key);
        if (value == null) {
            return fallback;
        }
        String normalized = String.valueOf(value).trim();
        return normalized.isEmpty() ? fallback : normalized;
    }

    private SourceDocumentDto toSourceDocumentDto(SourceDocument sourceDocument) {
        return new SourceDocumentDto(
            sourceDocument.getId(),
            sourceDocument.getExternalId(),
            sourceDocument.getProvider(),
            sourceDocument.getSourceType(),
            sourceDocument.getReceivedAt(),
            sourceDocument.getCreatedAt(),
            sourceDocument.getUpdatedAt(),
            sourceDocument.getProcessingStatus().name()
        );
    }

    private ExtractionDto toExtractionDto(Extraction extraction) {
        return new ExtractionDto(
            extraction.getId(),
            extraction.getSummary(),
            extraction.getConfidence(),
            extraction.getModel(),
            extraction.getProvider(),
            extraction.getPromptVersion()
        );
    }

    private DomainActionDto toActionDto(Action action) {
        return new DomainActionDto(
            action.getId(),
            action.getClass().getSimpleName().toUpperCase(),
            action.getTitle(),
            action.getDescription(),
            action.getStatus(),
            action.getConfidence()
        );
    }
}
