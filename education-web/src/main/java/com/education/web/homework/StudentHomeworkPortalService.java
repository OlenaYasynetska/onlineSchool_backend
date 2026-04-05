package com.education.web.homework;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.SchoolGroupEntity;
import com.education.web.auth.model.SchoolGroupStudentEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.repository.SchoolGroupJpaRepository;
import com.education.web.auth.repository.SchoolGroupStudentJpaRepository;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.homework.dto.HomeworkSubmissionResponse;
import com.education.web.homework.dto.StudentGroupOptionResponse;
import com.education.web.homework.dto.TeacherOptionShortResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentHomeworkPortalService {

    private final SpringDataStudentJpaRepository students;
    private final TeacherJpaRepository teachers;
    private final SchoolGroupStudentJpaRepository groupStudents;
    private final SchoolGroupJpaRepository schoolGroups;
    private final HomeworkPortalSubmissionJpaRepository submissions;

    private final Path uploadRoot;

    public StudentHomeworkPortalService(
            SpringDataStudentJpaRepository students,
            TeacherJpaRepository teachers,
            SchoolGroupStudentJpaRepository groupStudents,
            SchoolGroupJpaRepository schoolGroups,
            HomeworkPortalSubmissionJpaRepository submissions,
            @Value("${app.homework-upload.dir:uploads/homework}") String uploadDir
    ) {
        this.students = students;
        this.teachers = teachers;
        this.groupStudents = groupStudents;
        this.schoolGroups = schoolGroups;
        this.submissions = submissions;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public List<TeacherOptionShortResponse> listTeachersForStudent(String userId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        return teachers.findAllBySchoolIdWithUserOrderByName(st.getSchoolId()).stream()
                .map(t -> {
                    var u = t.getUser();
                    String dn = (u.getFirstName() + " " + u.getLastName()).trim();
                    return new TeacherOptionShortResponse(t.getId(), dn);
                })
                .collect(Collectors.toList());
    }

    public List<StudentGroupOptionResponse> listGroupsForStudent(String userId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        return groupStudents.findByStudentId(st.getId()).stream()
                .map(SchoolGroupStudentEntity::getGroup)
                .map(g -> new StudentGroupOptionResponse(g.getId(), g.getName(), g.getCode()))
                .collect(Collectors.toList());
    }

    public List<HomeworkSubmissionResponse> listMySubmissions(String userId) {
        StudentJpaEntity st = requireStudentByUser(userId);
        return submissions.findByStudentIdOrderBySubmittedAtDesc(st.getId()).stream()
                .map(s -> toResponse(s, st.getFullName(), st.getEmail()))
                .collect(Collectors.toList());
    }

    @Transactional
    public HomeworkSubmissionResponse submit(
            String userId,
            String teacherId,
            String groupId,
            String subjectTitle,
            String messageText,
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        StudentJpaEntity student = requireStudentByUser(userId);
        TeacherEntity teacher = teachers.findById(teacherId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found")
        );
        if (!teacher.getSchool().getId().equals(student.getSchoolId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher is not from your school");
        }
        String gid = groupId != null ? groupId.trim() : "";
        String groupIdToStore = null;
        if (!gid.isBlank()) {
            if (!groupStudents.existsByStudentIdAndGroup_Id(student.getId(), gid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not enrolled in this group");
            }
            groupIdToStore = gid;
        }
        String subj = subjectTitle != null ? subjectTitle.trim() : "";
        if (subj.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is required");
        }

        String orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String safe = safeFileName(orig);
        String submissionId = UUID.randomUUID().toString();
        String storedFileName = UUID.randomUUID() + "_" + safe;
        String relativePath = submissionId + "/" + storedFileName;

        HomeworkPortalSubmissionEntity row = new HomeworkPortalSubmissionEntity();
        row.setId(submissionId);
        row.setStudentId(student.getId());
        row.setTeacherId(teacher.getId());
        row.setGroupId(groupIdToStore);
        row.setSubjectTitle(subj);
        row.setMessageText(messageText != null ? messageText.trim() : null);
        row.setFileName(orig);
        row.setStoragePath(relativePath);
        row.setContentType(file.getContentType());
        row.setFileSizeBytes(file.getSize());
        row.setStatus("submitted");
        row.setSubmittedAt(Instant.now());

        Path target = uploadRoot.resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save file: " + e.getMessage()
            );
        }

        submissions.save(row);
        return toResponse(row, student.getFullName(), student.getEmail());
    }

    private StudentJpaEntity requireStudentByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }
        return students.findByUserId(userId.trim()).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not linked to this account (user_id in students)"
                )
        );
    }

    private HomeworkSubmissionResponse toResponse(
            HomeworkPortalSubmissionEntity s,
            String studentName,
            String studentEmail
    ) {
        String groupName = null;
        if (s.getGroupId() != null) {
            groupName = schoolGroups.findById(s.getGroupId()).map(SchoolGroupEntity::getName).orElse(null);
        }
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

    private static String safeFileName(String name) {
        String n = name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        if (n.isBlank()) {
            n = "file";
        }
        if (n.length() > 200) {
            n = n.substring(0, 200);
        }
        return n;
    }
}
