package com.familyos.familyos.storage.dto;

import java.util.List;

public record StorageSettingsDto(
        List<StorageConnectedAccountDto> connectedAccounts
) {}
