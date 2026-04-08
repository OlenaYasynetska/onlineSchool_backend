package com.education.web.homework;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupStudentEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.homework.dto.GradeHomeworkRequest;
import com.education.web.homework.dto.HomeworkSubmissionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeacherHomeworkPortalService {

    private final TeacherJpaRepository teachers;
    private final HomeworkPortalSubmissionJpaRepository submissions;
    private final SpringDataStudentJpaRepository students;
    private final SchoolGroupJpaRepository schoolGroups;
    private final SchoolGroupStudentJpaRepository groupStudents;
    private final Path uploadRoot;

    public TeacherHomeworkPortalService(
            TeacherJpaRepository teachers,
            HomeworkPortalSubmissionJpaRepository submissions,
            SpringDataStudentJpaRepository students,
            SchoolGroupJpaRepository schoolGroups,
            SchoolGroupStudentJpaRepository groupStudents,
            @Value("${app.homework-upload.dir:uploads/homework}") String uploadDir
    ) {
        this.teachers = teachers;
        this.submissions = submissions;
        this.students = students;
        this.schoolGroups = schoolGroups;
        this.groupStudents = groupStudents;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionResponse> listPending(String teacherUserId) {
        TeacherEntity t = requireTeacher(teacherUserId);
        return submissions
                .findByTeacherIdAndStatusOrderBySubmittedAtDesc(t.getId(), "submitted")
                .stream()
                .map(this::toResponseWithStudent)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionResponse> listMyGraded(String teacherUserId) {
        TeacherEntity t = requireTeacher(teacherUserId);
        return submissions.findByTeacherIdAndStatusOrderBySubmittedAtDesc(t.getId(), "graded")
                .stream()
                .map(this::toResponseWithStudent)
                .collect(Collectors.toList());
    }

    @Transactional
    public HomeworkSubmissionResponse grade(String teacherUserId, String submissionId, GradeHomeworkRequest req) {
        TeacherEntity teacher = requireTeacher(teacherUserId);
        HomeworkPortalSubmissionEntity s = submissions.findById(submissionId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")
        );
        if (!teacher.getId().equals(s.getTeacherId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your submission");
        }
        if (!"submitted".equals(s.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already graded or invalid status");
        }
        int stars = req.stars();
        s.setStars(stars);
        s.setTeacherFeedback(req.feedback() != null ? req.feedback().trim() : null);
        s.setGradedAt(Instant.now());
        s.setGradedByTeacherId(teacher.getId());
        s.setStatus("graded");
        submissions.save(s);

        if (s.getGroupId() != null) {
            schoolGroups.findById(s.getGroupId()).ifPresent(g -> {
                g.setHomeworkStarsTotal(g.getHomeworkStarsTotal() + stars);
                schoolGroups.save(g);
            });
        }

        StudentJpaEntity st = students.findById(s.getStudentId()).orElseThrow();
        return toResponse(s, st.getFullName(), st.getEmail());
    }

    public record FileDownload(Resource resource, String downloadFileName) {
    }

    public FileDownload getFileDownload(String teacherUserId, String submissionId) {
        TeacherEntity teacher = requireTeacher(teacherUserId);
        HomeworkPortalSubmissionEntity s = submissions.findById(submissionId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")
        );
        if (!teacher.getId().equals(s.getTeacherId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your submission");
        }
        Path file = uploadRoot.resolve(s.getStoragePath());
        if (!file.normalize().startsWith(uploadRoot.normalize())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid path");
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File missing on server");
            }
            return new FileDownload(resource, s.getFileName());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private TeacherEntity requireTeacher(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        return teachers.findByUser_Id(userId.trim()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher profile not found")
        );
    }

    private HomeworkSubmissionResponse toResponseWithStudent(HomeworkPortalSubmissionEntity s) {
        StudentJpaEntity st = students.findById(s.getStudentId()).orElseGet(() -> {
            StudentJpaEntity fallback = new StudentJpaEntity();
            fallback.setFullName("—");
            fallback.setEmail("");
            return fallback;
        });
        return toResponse(s, st.getFullName(), st.getEmail());
    }

    private HomeworkSubmissionResponse toResponse(
            HomeworkPortalSubmissionEntity s,
            String studentName,
            String studentEmail
    ) {
        String groupName = resolveGroupDisplayName(s.getGroupId(), s.getStudentId(), s.getTeacherId());
        return new HomeworkSubmissionResponse(
                s.getId(),
                studentName,
                studentEmail,
                s.getSubjectTitle(),
                s.getMessageText(),
                s.getFileName(),
                s.getStatus(),
                s.getStars(),
                s.getTeacherFeedback(),
                groupName,
                s.getSubmittedAt(),
                s.getGradedAt()
        );
    }

    /**
     * Назва групи з рядка здачі або з записів учня в {@code school_group_students}.
     * Якщо {@code group_id} порожній — показуємо групи, де учень зарахований і де викладач цього ДЗ
     * призначений на групу; інакше всі групи учня (через кому).
     */
    private String resolveGroupDisplayName(String submissionGroupId, String studentId, String teacherId) {
        if (submissionGroupId != null && !submissionGroupId.isBlank()) {
            return schoolGroups.findById(submissionGroupId).map(SchoolGroupEntity::getName).orElse(null);
        }
        List<SchoolGroupStudentEntity> memberships = groupStudents.findByStudentIdFetchGroup(studentId);
        if (memberships.isEmpty()) {
            return null;
        }
        Set<String> teacherGroupIds = schoolGroups.findByTeacher_IdOrderByNameAsc(teacherId).stream()
                .map(SchoolGroupEntity::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> forThisTeacher = memberships.stream()
                .map(SchoolGroupStudentEntity::getGroup)
                .filter(g -> teacherGroupIds.contains(g.getId()))
                .map(SchoolGroupEntity::getName)
                .sorted()
                .distinct()
                .toList();
        if (!forThisTeacher.isEmpty()) {
            return String.join(", ", forThisTeacher);
        }
        return memberships.stream()
                .map(m -> m.getGroup().getName())
                .sorted()
                .distinct()
                .collect(Collectors.joining(", "));
    }
}
