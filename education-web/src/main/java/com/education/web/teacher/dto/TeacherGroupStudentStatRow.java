package com.education.web.teacher.dto;

import java.util.Map;

/** Один учень у групі: показник за предметами (sum або average — залежить від школи). */
public record TeacherGroupStudentStatRow(
        String studentId,
        String fullName,
        Map<String, Double> starsBySubject
) {
}
