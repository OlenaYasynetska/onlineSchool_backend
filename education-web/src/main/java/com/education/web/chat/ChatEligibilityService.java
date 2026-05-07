package com.education.web.chat;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Перевірка ролі та зв’язку «вчитель призначений на групу учня» (MySQL).
 */
@Service
public class ChatEligibilityService {

    private final UserJpaRepository users;
    private final TeacherJpaRepository teachers;
    private final SpringDataStudentJpaRepository students;
    private final ChatTeacherStudentLinkService linkService;

    public ChatEligibilityService(
            UserJpaRepository users,
            TeacherJpaRepository teachers,
            SpringDataStudentJpaRepository students,
            ChatTeacherStudentLinkService linkService) {
        this.users = users;
        this.teachers = teachers;
        this.students = students;
        this.linkService = linkService;
    }

    public ResolvedChatParticipants resolveForOpenChat(
            String currentUserId,
            String peerEntityId,
            String peerKindRaw) {
        UserEntity me = users.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserRole role = me.getRole();
        if (role != UserRole.TEACHER && role != UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat is only for teachers and students");
        }

        String peerKind = peerKindRaw == null ? "" : peerKindRaw.trim().toLowerCase();
        if ("teacher".equals(peerKind)) {
            if (role != UserRole.STUDENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "peerKind=teacher is for student accounts");
            }
            return resolveStudentFromTeacherPeer(currentUserId, peerEntityId);
        }
        if ("student".equals(peerKind)) {
            if (role != UserRole.TEACHER) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "peerKind=student is for teacher accounts");
            }
            return resolveTeacherFromStudentPeer(currentUserId, peerEntityId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "peerKind must be teacher or student");
    }

    private ResolvedChatParticipants resolveStudentFromTeacherPeer(String studentUserId, String teacherRecordId) {
        StudentJpaEntity student = students.findByUserId(studentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile not linked"));
        if (student.getUserId() == null || student.getUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student has no user account");
        }

        TeacherEntity teacher = teachers.findById(teacherRecordId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        if (!student.getSchoolId().equals(teacher.getSchool().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not in the same school");
        }
        if (!linkService.sharesGroup(teacher.getId(), student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No shared group with this teacher");
        }

        return new ResolvedChatParticipants(
                student.getSchoolId(),
                teacher.getId(),
                student.getId(),
                teacher.getUser().getId(),
                student.getUserId());
    }

    private ResolvedChatParticipants resolveTeacherFromStudentPeer(String teacherUserId, String studentRecordId) {
        TeacherEntity teacher = teachers.findByUser_Id(teacherUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher profile not found"));

        StudentJpaEntity student = students.findById(studentRecordId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (student.getUserId() == null || student.getUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student has no login");
        }

        if (!student.getSchoolId().equals(teacher.getSchool().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not in the same school");
        }
        if (!linkService.sharesGroup(teacher.getId(), student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student is not in your groups");
        }

        return new ResolvedChatParticipants(
                student.getSchoolId(),
                teacher.getId(),
                student.getId(),
                teacher.getUser().getId(),
                student.getUserId());
    }

    public void ensureParticipantUser(String userId, ResolvedChatParticipants participants) {
        if (!userId.equals(participants.teacherUserId()) && !userId.equals(participants.studentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }
    }

    public record ResolvedChatParticipants(
            String schoolId,
            String teacherRecordId,
            String studentRecordId,
            String teacherUserId,
            String studentUserId
    ) {
    }
}
