package com.education.web.schooladmin.dto;

import lombok.Data;

/**
 * Запит на створення групи (класу) для конкретної школи.
 *
 * Дати очікуємо у форматі {@code dd.MM.yyyy} (як на фронті в модалці).
 * <p>
 * Звичайний клас (не record): у JSON поле {@code groupId} може бути відсутнє — Jackson встановлює {@code null},
 * тоді як record + {@code Optional} часто ламає десеріалізацію без усіх полів.
 */
@Data
public class CreateSchoolGroupRequest {

    private String name;
    private String code;
    /** Опційно: предмет з таблиці {@code school_subjects} цієї школи. */
    private String subjectId;
    /** Опційно: викладач з таблиці {@code teachers} цієї школи. */
    private String teacherId;
    private String topicsLabel;
    private String startDate;
    private String endDate;
    private int studentsCount;
    private boolean active;
    /** Якщо null — вважаємо true (старі клієнти). */
    private Boolean showSubjectOnCard;
    /**
     * Якщо задано — оновити існуючу групу за id (редагування в UI).
     * Інакше upsert за парою (organization_id, code).
     */
    private String groupId;
}
