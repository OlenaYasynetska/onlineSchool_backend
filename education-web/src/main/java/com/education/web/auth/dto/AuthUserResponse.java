package com.education.web.auth.dto;

public record AuthUserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String role,
        String schoolId,
        String avatarUrl,
        String createdAt,
        String updatedAt
) {
}

