package com.familyos.familyos.storage.dto;

import java.util.List;

public record StorageConfigurationDto(
        List<StorageRuleDto> emailRules,
        List<StorageRuleDto> calendarRules,
        List<StoragePromptDto> prompts
) {}
