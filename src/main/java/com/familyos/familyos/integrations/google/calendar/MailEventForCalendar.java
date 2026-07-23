package com.familyos.familyos.integrations.google.calendar;

public record MailEventForCalendar(
    String title,
    String start,
    String end,
    String timezone,
    String location,
    String description
) {}
