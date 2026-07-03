package com.familyos.familyos.integrations.google.calendar;

import java.util.List;

public interface GoogleCalendarClient {
    List<GoogleCalendarEvent> fetchEvents(String accessToken, int maxResults);
}
