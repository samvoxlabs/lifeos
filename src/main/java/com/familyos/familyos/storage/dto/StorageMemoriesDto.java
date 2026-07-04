package com.familyos.familyos.storage.dto;

import java.util.List;
import java.util.Map;

public record StorageMemoriesDto(
        List<Map<String, Object>> items
) {}
