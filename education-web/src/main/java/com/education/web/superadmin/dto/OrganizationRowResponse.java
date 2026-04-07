package com.education.web.superadmin.dto;

public record OrganizationRowResponse(
        String id,
        String name,
        String plan,
        String status,
        String nextBilling,
        String registered,
        String totalReceived,
        String address,
        /** Кількість учнів школи з таблиці {@code students.school_id}. */
        int studentCount
) {
}

