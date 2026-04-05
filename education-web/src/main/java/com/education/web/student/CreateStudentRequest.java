package com.education.web.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Створення учня з обліковим записом (роль STUDENT) та опційним листом-запрошенням.
 */
public record CreateStudentRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String schoolId,
        /** null або порожньо — згенерувати випадковий пароль. */
        String password,
        /** За замовчуванням true — надіслати лист з паролем і посиланням на вхід. */
        Boolean sendInviteEmail
) {
}
