package com.familyos.familyos.storage.dto;

public record StorageBundleDto(
        StorageManifestDto manifest,
        StorageProfileDto profile,
        StorageSettingsDto settings,
        StorageConfigurationDto configuration,
        StorageKnowledgeDto knowledge,
        StorageIntegrationsDto integrations
) {}
