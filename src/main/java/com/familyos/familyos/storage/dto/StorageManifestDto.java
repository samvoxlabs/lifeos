package com.familyos.familyos.storage.dto;

import java.util.List;

public record StorageManifestDto(
        int schemaVersion,
        String lifeOSVersion,
        String lastSaved,
        List<String> modules
) {}
