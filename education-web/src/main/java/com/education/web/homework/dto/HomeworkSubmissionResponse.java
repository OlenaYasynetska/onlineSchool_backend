package com.education.web.homework.dto;

import java.time.Instant;

public record HomeworkSubmissionResponse(
        String id,
        String studentName,
        String studentEmail,
        String subjectTitle,
        String messageText,
        String fileName,
        String status,
        Integer stars,
        String teacherFeedback,
        String groupName,
        Instant submittedAt,
        Instant gradedAt,
        String homeworkNumber
) {
}
