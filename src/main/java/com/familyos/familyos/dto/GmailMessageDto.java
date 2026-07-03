package com.familyos.familyos.dto;

public record GmailMessageDto(
  String id,
  String threadId,
  String from,
  String subject,
  String date,
  String snippet
) {}
