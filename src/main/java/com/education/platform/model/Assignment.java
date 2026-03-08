package com.education.platform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "assignments")
public class Assignment {

    @Id
    private String id;

    @Indexed
    private String lessonId;
    @Indexed
    private String studentId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String status;  // PENDING, SUBMITTED, GRADED
    private String grade;

    private Instant createdAt;
    private Instant updatedAt;
}
