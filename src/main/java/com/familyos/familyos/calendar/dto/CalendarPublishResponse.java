package com.familyos.familyos.calendar.dto;

import java.util.List;

public record CalendarPublishResponse(String status, int eventsPublished, List<String> publishedEventIds, String publishedAt) {}
