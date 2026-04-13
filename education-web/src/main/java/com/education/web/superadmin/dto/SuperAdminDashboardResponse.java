package com.education.web.superadmin.dto;

import java.util.List;

public record SuperAdminDashboardResponse(
        List<PlanOverviewItemResponse> planOverview,
        List<SchoolCardResponse> schools,
        List<OrganizationRowResponse> organizations,
        List<PaymentHistoryRowResponse> payments,
        PlatformSummaryResponse summary
) {
}

