package com.education.web.schooladmin.dto;

public record SchoolAdminDashboardStatsResponse(
        int totalStudents,
        int totalPayments,
        int paidPayments,
        String totalReceived
) {
}

