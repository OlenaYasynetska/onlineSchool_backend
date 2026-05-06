package com.education.web.schooladmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Створює рядок у {@code teachers} для вже існуючого {@code users} з роллю TEACHER,
 * у якого ще немає профілю викладача (часта ситуація після ручного створення акаунта).
 */
public record AttachExistingTeacherRequest(
        @NotBlank @Email String email,
        List<@Size(max = 255) String> subjects
) {}
