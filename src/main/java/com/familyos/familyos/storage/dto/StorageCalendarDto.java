package com.familyos.familyos.storage.dto;

import java.util.List;
import java.util.Map;

public record StorageCalendarDto(
        List<Map<String, Object>> events
) {}
