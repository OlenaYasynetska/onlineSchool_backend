package com.education.web.superadmin.dto;

public record SchoolAdminContactResponse(
        String userId,
        String fullName,
        String schoolName,
        String email,
        String login,
        String registeredAt,
        /** Внутрішні нотатки суперадміна про цього адміністратора. */
        String notes
) {
}
