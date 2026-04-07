package com.education.web.homework;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeworkPortalSubmissionJpaRepository extends JpaRepository<HomeworkPortalSubmissionEntity, String> {

    List<HomeworkPortalSubmissionEntity> findByTeacherIdAndStatusOrderBySubmittedAtDesc(
            String teacherId,
            String status
    );

    List<HomeworkPortalSubmissionEntity> findByStudentIdOrderBySubmittedAtDesc(String studentId);

    List<HomeworkPortalSubmissionEntity> findByTeacherIdAndGroupIdAndStatus(
            String teacherId,
            String groupId,
            String status
    );
}
