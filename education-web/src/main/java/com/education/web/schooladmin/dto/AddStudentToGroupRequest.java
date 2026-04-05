package com.education.web.schooladmin.dto;

import jakarta.validation.constraints.NotBlank;

public record AddStudentToGroupRequest(@NotBlank String studentId) {
}
