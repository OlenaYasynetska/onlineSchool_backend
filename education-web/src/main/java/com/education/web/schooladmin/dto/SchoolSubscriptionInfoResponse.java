package com.education.web.schooladmin.dto;

/**
 * Тариф і дата закінчення доступу до платформи (організація / школа).
 */
public record SchoolSubscriptionInfoResponse(
        String planTitle,
        String platformAccessEndDate
) {
}
