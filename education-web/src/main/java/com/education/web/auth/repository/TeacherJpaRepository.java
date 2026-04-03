package com.education.web.auth.repository;

import com.education.web.auth.model.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherJpaRepository extends JpaRepository<TeacherEntity, String> {

    @Query(
            "SELECT t FROM TeacherEntity t JOIN FETCH t.user u WHERE t.school.id = :schoolId "
                    + "ORDER BY u.lastName ASC, u.firstName ASC"
    )
    List<TeacherEntity> findAllBySchoolIdWithUserOrderByName(@Param("schoolId") String schoolId);

    Optional<TeacherEntity> findByIdAndSchool_Id(String id, String schoolId);
}
