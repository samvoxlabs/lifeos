package com.familyos.familyos.demo.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.calendar.dto.CalendarResetResponse;
import com.familyos.familyos.calendar.dto.CalendarPublishResponse;
import com.familyos.familyos.common.ApiResponse;
import com.familyos.familyos.dto.CalendarEventDto;
import com.familyos.familyos.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/demo/calendar")
public class DemoCalendarController {

    private final CalendarService calendarService;
    private final AuthenticationService authenticationService;

    public DemoCalendarController(CalendarService calendarService, AuthenticationService authenticationService) {
        this.calendarService = calendarService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<CalendarPublishResponse>> publish() {
        String userId = authenticationService.currentUser().id();
        CalendarPublishResponse response = calendarService.publishEvents(userId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<CalendarEventDto>>> events() {
        String userId = authenticationService.currentUser().id();
        List<CalendarEventDto> events = calendarService.readUpcomingEvents(userId);
        return ResponseEntity.ok(ApiResponse.of(events));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<CalendarResetResponse>> reset() {
        String userId = authenticationService.currentUser().id();
        CalendarResetResponse response = calendarService.resetPublishedEvents(userId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
