package com.education.web.schooladmin.dto;

public record SchoolGroupCardResponse(
        String id,
        String name,
        String code,
        String subjectId,
        String teacherId,
        /** Ім'я з users (first + last), якщо група прив'язана до викладача. */
        String teacherDisplayName,
        String topicsLabel,
        String startDate,
        String endDate,
        int studentsCount,
        /** Сумарні зірки за ДЗ усіх учнів групи. */
        int homeworkStarsTotal,
        boolean active,
        boolean showSubjectOnCard
) {
}
