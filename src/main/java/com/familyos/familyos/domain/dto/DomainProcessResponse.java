package com.familyos.familyos.domain.dto;

import java.util.List;

public record DomainProcessResponse(
    SourceDocumentDto sourceDocument,
    ExtractionDto extraction,
    List<DomainActionDto> actions
) {}
