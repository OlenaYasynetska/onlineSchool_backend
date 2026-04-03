package com.education.web.schooladmin;

import com.education.web.schooladmin.dto.SchoolAdminDashboardResponse;
import com.education.web.schooladmin.service.SchoolAdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school-admin")
public class SchoolAdminDashboardController {
    private final SchoolAdminDashboardService dashboardService;

    public SchoolAdminDashboardController(SchoolAdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public SchoolAdminDashboardResponse dashboard(@RequestParam("schoolId") String schoolId) {
        return dashboardService.getDashboard(schoolId);
    }
}

