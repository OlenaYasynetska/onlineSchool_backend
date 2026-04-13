package com.education.web.superadmin.dto;

import jakarta.validation.constraints.NotBlank;

public record SchoolAdminUpdateRequest(
        @NotBlank String fullName,
        String schoolName,
        String email,
        String login,
        String notes
) {
}
