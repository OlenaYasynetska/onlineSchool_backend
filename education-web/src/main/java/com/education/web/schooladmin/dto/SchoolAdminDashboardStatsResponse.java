package com.education.web.schooladmin.dto;

public record SchoolAdminDashboardStatsResponse(
        int totalStudents,
        int totalTeachers,
        int totalGroups,
        int totalSubjects,
        int totalPayments,
        int paidPayments,
        String totalReceived
) {
}

