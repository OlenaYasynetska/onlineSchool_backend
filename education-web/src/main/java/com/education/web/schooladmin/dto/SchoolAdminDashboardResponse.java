package com.education.web.schooladmin.dto;

import java.util.List;

public record SchoolAdminDashboardResponse(
        /** ID організації (школи); дублює query `schoolId` для фронту. */
        String schoolId,
        SchoolAdminDashboardStatsResponse stats,
        List<StudentRowResponse> students,
        List<PaymentHistoryRowResponse> payments,
        SchoolSubscriptionInfoResponse subscription,
        List<SchoolGroupCardResponse> groups
) {
}

