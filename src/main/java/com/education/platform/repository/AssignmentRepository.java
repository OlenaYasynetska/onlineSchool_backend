package com.education.platform.repository;

import com.education.platform.model.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssignmentRepository extends MongoRepository<Assignment, String> {

    List<Assignment> findByLessonId(String lessonId);

    List<Assignment> findByStudentId(String studentId);
}
