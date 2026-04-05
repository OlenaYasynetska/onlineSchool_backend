package com.education.web.schooladmin.dto;

import java.util.List;

/**
 * Створення викладача: новий {@code users} (роль TEACHER) + рядок у {@code teachers}.
 * Предмети — окремі рядки в {@code teacher_subjects}.
 */
public record CreateSchoolTeacherRequest(
        String email,
        String firstName,
        String lastName,
        /** null або порожньо — згенерувати випадковий пароль. */
        String password,
        /** Назви предметів; кожен елемент — окремий рядок у {@code teacher_subjects}. */
        List<String> subjects,
        /** Опційно; зберігається в {@code users.phone}. */
        String phone,
        /** За замовчуванням true — лист із посиланням на вхід і паролем. */
        Boolean sendInviteEmail
) {
}
