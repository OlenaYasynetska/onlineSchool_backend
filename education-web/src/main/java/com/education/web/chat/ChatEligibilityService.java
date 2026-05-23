package com.education.web.chat;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.chat.document.ChatConversationDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Перевірка ролі та доступу до чату: та сама школа; учень–учень і вчитель–вчитель без обмеження спільної групи.
 */
@Service
@ConditionalOnProperty(name = "education.chat.mongodb-enabled", havingValue = "true")
public class ChatEligibilityService {

    private final UserJpaRepository users;
    private final TeacherJpaRepository teachers;
    private final SpringDataStudentJpaRepository students;

    public ChatEligibilityService(
            UserJpaRepository users,
            TeacherJpaRepository teachers,
            SpringDataStudentJpaRepository students) {
        this.users = users;
        this.teachers = teachers;
        this.students = students;
    }

    public ResolvedChatParticipants participantsFromConversation(ChatConversationDocument c) {
        if (Boolean.TRUE.equals(c.getTeacherPeerChat())) {
            return ResolvedChatParticipants.teacherPeers(
                    c.getSchoolId(),
                    c.getTeacherPeerLowRecordId(),
                    c.getTeacherPeerHighRecordId(),
                    c.getTeacherPeerLowUserId(),
                    c.getTeacherPeerHighUserId());
        }
        if (Boolean.TRUE.equals(c.getStudentPeerChat())) {
            return ResolvedChatParticipants.studentPeers(
                    c.getSchoolId(),
                    c.getStudentPeerLowRecordId(),
                    c.getStudentPeerHighRecordId(),
                    c.getStudentPeerLowUserId(),
                    c.getStudentPeerHighUserId());
        }
        return ResolvedChatParticipants.teacherStudent(
                c.getSchoolId(),
                c.getTeacherRecordId(),
                c.getStudentRecordId(),
                c.getTeacherUserId(),
                c.getStudentUserId());
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
            if (role == UserRole.STUDENT) {
                return resolveStudentFromTeacherPeer(currentUserId, peerEntityId);
            }
            if (role == UserRole.TEACHER) {
                return resolveTeacherToTeacherPeer(currentUserId, peerEntityId);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "peerKind=teacher is for teachers or students");
        }
        if ("student".equals(peerKind)) {
            if (role == UserRole.TEACHER) {
                return resolveTeacherFromStudentPeer(currentUserId, peerEntityId);
            }
            if (role == UserRole.STUDENT) {
                return resolveStudentToStudentPeer(currentUserId, peerEntityId);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "peerKind=student is for teachers or students");
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

        return ResolvedChatParticipants.teacherStudent(
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

        return ResolvedChatParticipants.teacherStudent(
                student.getSchoolId(),
                teacher.getId(),
                student.getId(),
                teacher.getUser().getId(),
                student.getUserId());
    }

    private ResolvedChatParticipants resolveStudentToStudentPeer(String studentUserId, String peerStudentRecordId) {
        StudentJpaEntity me = students.findByUserId(studentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile not linked"));
        if (me.getUserId() == null || me.getUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student has no user account");
        }
        StudentJpaEntity peer = students.findById(peerStudentRecordId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (peer.getUserId() == null || peer.getUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Peer student has no login yet");
        }
        if (peer.getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot chat with yourself");
        }
        if (!peer.getSchoolId().equals(me.getSchoolId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not in the same school");
        }

        String lowRec;
        String highRec;
        String lowUser;
        String highUser;
        if (me.getId().compareTo(peer.getId()) <= 0) {
            lowRec = me.getId();
            highRec = peer.getId();
            lowUser = me.getUserId();
            highUser = peer.getUserId();
        } else {
            lowRec = peer.getId();
            highRec = me.getId();
            lowUser = peer.getUserId();
            highUser = me.getUserId();
        }
        return ResolvedChatParticipants.studentPeers(me.getSchoolId(), lowRec, highRec, lowUser, highUser);
    }

    private ResolvedChatParticipants resolveTeacherToTeacherPeer(String teacherUserId, String peerTeacherRecordId) {
        TeacherEntity me = teachers.findByUser_Id(teacherUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher profile not found"));
        if (me.getUser() == null || me.getUser().getId() == null || me.getUser().getId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher has no user account");
        }
        TeacherEntity peer = teachers.findById(peerTeacherRecordId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));
        if (peer.getUser() == null || peer.getUser().getId() == null || peer.getUser().getId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Colleague has no login yet");
        }
        if (!me.getSchool().getId().equals(peer.getSchool().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not in the same school");
        }
        if (me.getId().equals(peer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot chat with yourself");
        }

        String lowRec;
        String highRec;
        String lowUser;
        String highUser;
        if (me.getId().compareTo(peer.getId()) <= 0) {
            lowRec = me.getId();
            highRec = peer.getId();
            lowUser = me.getUser().getId();
            highUser = peer.getUser().getId();
        } else {
            lowRec = peer.getId();
            highRec = me.getId();
            lowUser = peer.getUser().getId();
            highUser = me.getUser().getId();
        }
        return ResolvedChatParticipants.teacherPeers(me.getSchool().getId(), lowRec, highRec, lowUser, highUser);
    }

    public void ensureParticipantUser(String userId, ResolvedChatParticipants participants) {
        if (!participants.isParticipant(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }
    }

    public record ResolvedChatParticipants(
            String schoolId,
            boolean studentPeerChat,
            boolean teacherPeerChat,
            String teacherRecordId,
            String studentRecordId,
            String teacherUserId,
            String studentUserId,
            String studentPeerLowRecordId,
            String studentPeerHighRecordId,
            String studentPeerLowUserId,
            String studentPeerHighUserId,
            String teacherPeerLowRecordId,
            String teacherPeerHighRecordId,
            String teacherPeerLowUserId,
            String teacherPeerHighUserId) {

        public static ResolvedChatParticipants teacherStudent(
                String schoolId,
                String teacherRecordId,
                String studentRecordId,
                String teacherUserId,
                String studentUserId) {
            return new ResolvedChatParticipants(
                    schoolId,
                    false,
                    false,
                    teacherRecordId,
                    studentRecordId,
                    teacherUserId,
                    studentUserId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        public static ResolvedChatParticipants studentPeers(
                String schoolId,
                String studentPeerLowRecordId,
                String studentPeerHighRecordId,
                String studentPeerLowUserId,
                String studentPeerHighUserId) {
            return new ResolvedChatParticipants(
                    schoolId,
                    true,
                    false,
                    null,
                    null,
                    null,
                    null,
                    studentPeerLowRecordId,
                    studentPeerHighRecordId,
                    studentPeerLowUserId,
                    studentPeerHighUserId,
                    null,
                    null,
                    null,
                    null);
        }

        public static ResolvedChatParticipants teacherPeers(
                String schoolId,
                String teacherPeerLowRecordId,
                String teacherPeerHighRecordId,
                String teacherPeerLowUserId,
                String teacherPeerHighUserId) {
            return new ResolvedChatParticipants(
                    schoolId,
                    false,
                    true,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    teacherPeerLowRecordId,
                    teacherPeerHighRecordId,
                    teacherPeerLowUserId,
                    teacherPeerHighUserId);
        }

        public boolean isParticipant(String userId) {
            if (studentPeerChat) {
                return userId.equals(studentPeerLowUserId) || userId.equals(studentPeerHighUserId);
            }
            if (teacherPeerChat) {
                return userId.equals(teacherPeerLowUserId) || userId.equals(teacherPeerHighUserId);
            }
            return userId.equals(teacherUserId) || userId.equals(studentUserId);
        }
    }
}
