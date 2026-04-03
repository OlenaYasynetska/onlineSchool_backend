package com.education.web.schooladmin.dto;

import java.util.List;

public record SchoolAdminDashboardResponse(
        SchoolAdminDashboardStatsResponse stats,
        List<StudentRowResponse> students,
        List<PaymentHistoryRowResponse> payments,
        SchoolSubscriptionInfoResponse subscription,
        List<SchoolGroupCardResponse> groups
) {
}

