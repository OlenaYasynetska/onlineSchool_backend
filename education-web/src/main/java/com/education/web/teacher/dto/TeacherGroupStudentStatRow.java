package com.education.web.teacher.dto;

import java.util.Map;

/** Один учень у групі: зірки за предметами (ключ — назва з teacher_subjects). */
public record TeacherGroupStudentStatRow(
        String studentId,
        String fullName,
        Map<String, Integer> starsBySubject
) {
}
