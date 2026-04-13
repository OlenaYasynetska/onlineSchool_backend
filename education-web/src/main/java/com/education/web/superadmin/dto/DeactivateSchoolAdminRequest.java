package com.education.web.superadmin.dto;

import jakarta.validation.constraints.NotBlank;

public record DeactivateSchoolAdminRequest(@NotBlank String userId) {
}
