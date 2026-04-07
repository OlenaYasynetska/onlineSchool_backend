package com.education.web.teacher.dto;

import java.util.List;

/** Статистика по одній групі: предмети вчителя (узгоджені з групою) і рядки по учнях. */
public record TeacherGroupStatsResponse(
        String groupId,
        String groupName,
        String groupCode,
        List<String> subjectTitles,
        List<TeacherGroupStudentStatRow> students
) {
}
