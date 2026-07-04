package com.familyos.familyos.storage.dto;

import java.util.List;

public record StorageGmailDto(
        List<String> senders,
        List<String> subjects
) {}
