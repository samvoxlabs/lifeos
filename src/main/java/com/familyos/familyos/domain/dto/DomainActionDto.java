package com.familyos.familyos.domain.dto;

import java.util.UUID;

public record DomainActionDto(
    UUID id,
    String type,
    String title,
    String description,
    String status,
    Double confidence
) {}
