package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.dto.CalendarEventDto;
import com.familyos.familyos.service.CalendarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final AuthenticationService authenticationService;

    public CalendarController(CalendarService calendarService, AuthenticationService authenticationService) {
        this.calendarService = calendarService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/events")
    public List<CalendarEventDto> events() {
        return calendarService.readUpcomingEvents(authenticationService.currentUser().id());
    }
}
