package com.familyos.familyos.storage;

import com.familyos.familyos.dto.AuthenticatedUser;

import java.util.List;

public record StorageStatus(
        AuthenticatedUser currentUser,
        boolean initialized,
        int schemaVersion,
        List<String> availableModules,
        String lastBootstrap,
        String lastSave,
        boolean dirty
) {}
