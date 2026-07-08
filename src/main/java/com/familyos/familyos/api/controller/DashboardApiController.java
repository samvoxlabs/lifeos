package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.DashboardResponse;
import com.familyos.familyos.api.service.DashboardApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardApiService dashboardApiService;

    public DashboardApiController(DashboardApiService dashboardApiService) {
        this.dashboardApiService = dashboardApiService;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardApiService.getDashboard();
    }
}
