package com.familyos.familyos.storage.dto;

import com.familyos.familyos.dto.AuthenticatedUser;

public record StorageStatusDto(
        int schemaVersion,
        String lastLoaded,
        String lastSaved,
        boolean dirty,
        AuthenticatedUser currentUser
) {}
