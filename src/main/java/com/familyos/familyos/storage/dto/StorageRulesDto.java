package com.familyos.familyos.storage.dto;

import java.util.List;

public record StorageRulesDto(
        List<StorageRuleDto> emailRules,
        List<StorageRuleDto> calendarRules
) {}
