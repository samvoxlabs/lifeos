package com.familyos.familyos.dto;

import java.util.List;

public record GmailAllowlistDto(
        List<String> senders,
        List<String> subjects
) {}
