package com.education.web.teacher.dto;

import java.util.List;
import java.util.Map;

/**
 * Статистика по одній групі: предмети вчителя, зірки з оцінених ДЗ у БД,
 * і кумулятивний ряд для графіка по місяцях (останні N місяців від {@code graded_at}).
 */
public record TeacherGroupStatsResponse(
        String groupId,
        String groupName,
        String groupCode,
        List<String> subjectTitles,
        List<TeacherGroupStudentStatRow> students,
        List<String> chartMonthLabels,
        Map<String, List<Integer>> starsBySubjectChartSeries
) {
}
