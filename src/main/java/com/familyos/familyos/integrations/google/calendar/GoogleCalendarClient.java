package com.familyos.familyos.integrations.google.calendar;

import java.util.List;

public interface GoogleCalendarClient {
    List<GoogleCalendarEvent> fetchEvents(String accessToken, int maxResults);
    String createCalendarEvent(String accessToken, String calendarId, MailEventForCalendar event);
    void deleteCalendarEvent(String accessToken, String calendarId, String eventId);
}
