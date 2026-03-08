package com.education.platform.repository;

import com.education.platform.model.Teacher;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends MongoRepository<Teacher, String> {

    Optional<Teacher> findByUserId(String userId);

    List<Teacher> findBySchoolId(String schoolId);

    boolean existsByUserId(String userId);
}
