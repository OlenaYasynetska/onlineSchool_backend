package com.education.web.superadmin.dto;

public record PlanOverviewItemResponse(
        String id,
        String label,
        int count,
        int percent
) {
}

