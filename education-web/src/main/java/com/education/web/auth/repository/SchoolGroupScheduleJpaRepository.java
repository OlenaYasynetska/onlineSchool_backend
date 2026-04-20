package com.education.web.auth.repository;

import com.education.web.auth.model.SchoolGroupScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SchoolGroupScheduleJpaRepository extends JpaRepository<SchoolGroupScheduleEntity, String> {

    @Query(
            "SELECT DISTINCT s FROM SchoolGroupScheduleEntity s "
                    + "JOIN FETCH s.group g "
                    + "LEFT JOIN FETCH s.teacher t LEFT JOIN FETCH t.user "
                    + "LEFT JOIN FETCH s.subject "
                    + "LEFT JOIN FETCH g.teacher gt LEFT JOIN FETCH gt.user "
                    + "LEFT JOIN FETCH g.subject gs "
                    + "WHERE g.organization.id = :schoolId ORDER BY s.dayOfWeek, s.startTime"
    )
    List<SchoolGroupScheduleEntity> findAllForSchoolWithDetails(@Param("schoolId") String schoolId);

    @Query(
            "SELECT DISTINCT s FROM SchoolGroupScheduleEntity s "
                    + "JOIN FETCH s.group g LEFT JOIN FETCH g.teacher gt LEFT JOIN FETCH gt.user "
                    + "LEFT JOIN FETCH s.teacher t LEFT JOIN FETCH t.user "
                    + "LEFT JOIN FETCH s.subject LEFT JOIN FETCH g.subject gs "
                    + "WHERE g.id IN :groupIds ORDER BY s.dayOfWeek, s.startTime"
    )
    List<SchoolGroupScheduleEntity> findByGroup_IdInWithDetails(@Param("groupIds") Collection<String> groupIds);

    /**
     * Уроки вчителя: або {@code school_group_schedule.teacher_id}, або класний з групи, якщо в слоті teacher
     * не задано.
     */
    @Query(
            "SELECT DISTINCT s FROM SchoolGroupScheduleEntity s "
                    + "JOIN FETCH s.group g LEFT JOIN FETCH g.teacher gt LEFT JOIN FETCH gt.user "
                    + "LEFT JOIN FETCH s.teacher t LEFT JOIN FETCH t.user "
                    + "LEFT JOIN FETCH s.subject LEFT JOIN FETCH g.subject gs "
                    + "WHERE g.organization.id = :schoolId "
                    + "AND (t.id = :teacherId OR (t IS NULL AND gt IS NOT NULL AND gt.id = :teacherId)) "
                    + "ORDER BY s.dayOfWeek, s.startTime"
    )
    List<SchoolGroupScheduleEntity> findForTeacherInSchool(
            @Param("schoolId") String schoolId,
            @Param("teacherId") String teacherId
    );

    @Query(
            "SELECT s FROM SchoolGroupScheduleEntity s JOIN FETCH s.group g "
                    + "LEFT JOIN FETCH s.teacher t LEFT JOIN FETCH t.user "
                    + "LEFT JOIN FETCH s.subject "
                    + "WHERE s.id = :id AND g.organization.id = :schoolId"
    )
    Optional<SchoolGroupScheduleEntity> findOneByIdAndSchoolId(
            @Param("id") String id,
            @Param("schoolId") String schoolId
    );
}
