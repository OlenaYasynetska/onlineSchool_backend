package com.education.web.schooladmin.dto;

/**
 * Запит на створення групи (класу) для конкретної школи.
 *
 * Дати очікуємо у форматі `dd.MM.yyyy` (як на фронті в модалці).
 */
public record CreateSchoolGroupRequest(
        String name,
        String code,
        /** Опційно: предмет з таблиці `school_subjects` цієї школи. */
        String subjectId,
        /** Опційно: викладач з таблиці `teachers` цієї школи. */
        String teacherId,
        String topicsLabel,
        String startDate,
        String endDate,
        int studentsCount,
        boolean active,
        /** Якщо null — вважаємо true (старі клієнти). */
        Boolean showSubjectOnCard
) {
}

