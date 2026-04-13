package com.education.web.teacher.dto;

import java.util.List;
import java.util.Map;

/** Кумулятивні зірки з оцінених ДЗ вчителя по предметах у межах діапазону дат. */
public record TeacherHomeworkStarsChartResponse(
        List<String> bucketLabels,
        Map<String, List<Integer>> starsBySubjectSeries
) {
}
