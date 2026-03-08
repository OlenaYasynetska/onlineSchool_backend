package com.education.platform.repository;

import com.education.platform.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {

    List<Course> findBySchoolId(String schoolId);

    List<Course> findByTeacherId(String teacherId);
}
