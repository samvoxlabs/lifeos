package com.familyos.familyos.storage.dto;

import java.util.Map;

public record StorageKnowledgeItemDto(
        String id,
        String type,
        String title,
        String content,
        String source,
        Map<String, Object> metadata
) {}
