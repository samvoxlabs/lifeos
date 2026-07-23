package com.familyos.familyos.calendar.controller;

import com.familyos.familyos.calendar.dto.CalendarPublishResponse;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.dto.CalendarEventDto;
import com.familyos.familyos.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarDomainController {

    private final CalendarService calendarService;

    public CalendarDomainController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<CalendarEventDto>>> getCalendarEvents() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<CalendarEventDto> events = calendarService.readUpcomingEvents(userId);
        return ResponseEntity.ok(ApiResponse.of(events));
    }

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<CalendarPublishResponse>> publishEvents() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        CalendarPublishResponse response = calendarService.publishEvents(userId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
