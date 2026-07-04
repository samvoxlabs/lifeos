package com.familyos.familyos.domain.service;

import com.familyos.familyos.domain.dto.DomainActionDto;
import com.familyos.familyos.domain.dto.DomainProcessResponse;
import com.familyos.familyos.domain.dto.ProcessExtractionRequest;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;

import java.util.List;
import java.util.UUID;

public interface DomainPersistenceService {
    DomainProcessResponse process(ProcessExtractionRequest request);
    DomainProcessResponse processPersistedSourceDocument(UUID sourceDocumentId, boolean forceReprocess);
    void persistSourceDocument(NormalizedDocument sourceDocument);
    List<DomainActionDto> getTasks();
    List<DomainActionDto> getEvents();
    List<DomainActionDto> getReminders();
}
