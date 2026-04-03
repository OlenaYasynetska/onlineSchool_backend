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
        String password,
        /** Назви предметів; кожен елемент — окремий рядок у {@code teacher_subjects}. */
        List<String> subjects
) {
}
