package com.familyos.familyos.storage.dto;

import java.util.List;

public record StorageTokenDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        List<String> scopes,
        String expiresAt
) {}
