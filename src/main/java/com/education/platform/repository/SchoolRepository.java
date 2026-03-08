package com.education.platform.repository;

import com.education.platform.model.School;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SchoolRepository extends MongoRepository<School, String> {
}
