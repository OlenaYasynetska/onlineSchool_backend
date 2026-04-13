package com.education.web.homework;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HomeworkPortalSubmissionJpaRepository extends JpaRepository<HomeworkPortalSubmissionEntity, String> {

    /**
     * Явний native query: колонка {@code status} у MySQL може конфліктувати з резервованими іменами
     * у згенерованому JPQL/Hibernate для derived-методу.
     */
    @Query(
            value = "SELECT * FROM homework_portal_submissions WHERE teacher_id = :teacherId "
                    + "AND `status` = :status ORDER BY submitted_at DESC",
            nativeQuery = true
    )
    List<HomeworkPortalSubmissionEntity> findByTeacherIdAndStatusOrderBySubmittedAtDesc(
            @Param("teacherId") String teacherId,
            @Param("status") String status
    );

    List<HomeworkPortalSubmissionEntity> findByStudentIdOrderBySubmittedAtDesc(String studentId);

    List<HomeworkPortalSubmissionEntity> findByTeacherIdAndGroupIdAndStatus(
            String teacherId,
            String groupId,
            String status
    );
}
