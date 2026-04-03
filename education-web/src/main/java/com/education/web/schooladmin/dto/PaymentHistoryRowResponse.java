package com.education.web.schooladmin.dto;

public record PaymentHistoryRowResponse(
        String id,
        String date,
        String amount,
        String currency,
        String status
) {
}

