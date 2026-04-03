package com.education.web.superadmin.dto;

public record PaymentHistoryRowResponse(
        String id,
        String date,
        String organization,
        String amount,
        String status
) {
}

