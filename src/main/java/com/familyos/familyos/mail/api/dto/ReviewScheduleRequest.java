package com.familyos.familyos.mail.api.dto;

public record ReviewScheduleRequest(
    String mailbox,
    Integer maxMessages
) {}
