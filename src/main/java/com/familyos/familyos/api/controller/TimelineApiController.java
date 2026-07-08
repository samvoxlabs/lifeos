package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.TimelineItem;
import com.familyos.familyos.api.service.TimelineApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timeline")
public class TimelineApiController {

    private final TimelineApiService timelineApiService;

    public TimelineApiController(TimelineApiService timelineApiService) {
        this.timelineApiService = timelineApiService;
    }

    @GetMapping
    public List<TimelineItem> getTimeline() {
        return timelineApiService.getTimeline();
    }
}
