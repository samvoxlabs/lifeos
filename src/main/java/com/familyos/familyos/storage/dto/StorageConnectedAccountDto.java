package com.familyos.familyos.storage.dto;

public record StorageConnectedAccountDto(
        String provider,
        String providerAccountId,
        String email,
        String displayName,
        StorageTokenDto token
) {}
