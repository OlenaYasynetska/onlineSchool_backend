package com.education.web.superadmin;

import com.education.web.superadmin.dto.SuperAdminDashboardResponse;
import com.education.web.superadmin.service.SuperAdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminDashboardController {
    private final SuperAdminDashboardService dashboardService;

    public SuperAdminDashboardController(SuperAdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public SuperAdminDashboardResponse dashboard() {
        return dashboardService.getDashboard();
    }
}

