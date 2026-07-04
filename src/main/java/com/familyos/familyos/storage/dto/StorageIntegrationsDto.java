package com.familyos.familyos.storage.dto;

public record StorageIntegrationsDto(
        StorageSyncStateDto gmailSync,
        StorageSyncStateDto calendarSync
) {}
