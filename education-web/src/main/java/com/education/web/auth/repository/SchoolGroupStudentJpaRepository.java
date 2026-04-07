package com.education.web.auth.repository;

import com.education.web.auth.model.SchoolGroupStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SchoolGroupStudentJpaRepository extends JpaRepository<SchoolGroupStudentEntity, String> {

    boolean existsByStudentIdAndGroup_Id(String studentId, String groupId);

    long countByGroup_Id(String groupId);

    @Query("SELECT l.group.id, COUNT(l) FROM SchoolGroupStudentEntity l WHERE l.group.id IN :ids GROUP BY l.group.id")
    List<Object[]> countRowsByGroupIds(@Param("ids") Collection<String> ids);

    List<SchoolGroupStudentEntity> findByGroup_Organization_Id(String organizationId);

    List<SchoolGroupStudentEntity> findByStudentId(String studentId);

    List<SchoolGroupStudentEntity> findByGroup_IdOrderByStudentIdAsc(String groupId);
}
