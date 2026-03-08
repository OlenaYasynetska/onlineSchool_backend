package com.education.platform.repository;

import com.education.platform.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends MongoRepository<Student, String> {

    Optional<Student> findByUserId(String userId);

    List<Student> findBySchoolId(String schoolId);

    boolean existsByUserId(String userId);
}
