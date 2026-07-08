package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.EventResponse;
import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.service.EventApiService;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventApiController {

    private final EventApiService eventApiService;

    public EventApiController(EventApiService eventApiService) {
        this.eventApiService = eventApiService;
    }

    @GetMapping
    public PagedResponse<EventResponse> getEvents(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        Pageable pageable
    ) {
        return eventApiService.getEvents(from, to, pageable);
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable UUID id) {
        return eventApiService.getEvent(id);
    }
}
