package com.education.web.superadmin.dto;

public record SchoolCardResponse(
        String id,
        String title,
        String displayName,
        String address,
        String plan,
        int studentCount
) {
}
