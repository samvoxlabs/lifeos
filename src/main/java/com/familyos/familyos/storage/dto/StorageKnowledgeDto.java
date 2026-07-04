package com.familyos.familyos.storage.dto;

import java.util.List;

public record StorageKnowledgeDto(
        List<StorageKnowledgeItemDto> summaries,
        List<StorageKnowledgeItemDto> actionItems,
        List<StorageKnowledgeItemDto> reminders,
        List<StorageKnowledgeItemDto> relationships,
        List<StorageKnowledgeItemDto> memories
) {}
