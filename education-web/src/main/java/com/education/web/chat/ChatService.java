package com.education.web.chat;

import com.education.infrastructure.student.SpringDataStudentJpaRepository;
import com.education.infrastructure.student.StudentJpaEntity;
import com.education.web.auth.model.TeacherEntity;
import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.TeacherJpaRepository;
import com.education.web.auth.repository.UserJpaRepository;
import com.education.web.chat.document.ChatConversationDocument;
import com.education.web.chat.document.ChatMessageDocument;
import com.education.web.chat.dto.ChatConversationSummaryResponse;
import com.education.web.chat.dto.ChatMessageResponse;
import com.education.web.chat.dto.OpenConversationResponse;
import com.education.web.chat.repository.ChatConversationRepository;
import com.education.web.chat.repository.ChatMessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
@ConditionalOnProperty(name = "education.chat.mongodb-enabled", havingValue = "true")
public class ChatService {

    private static final int PREVIEW_MAX = 240;

    private final ChatConversationRepository conversations;
    private final ChatMessageRepository messages;
    private final ChatEligibilityService eligibility;
    private final UserJpaRepository users;
    private final TeacherJpaRepository teachers;
    private final SpringDataStudentJpaRepository students;

    public ChatService(
            ChatConversationRepository conversations,
            ChatMessageRepository messages,
            ChatEligibilityService eligibility,
            UserJpaRepository users,
            TeacherJpaRepository teachers,
            SpringDataStudentJpaRepository students) {
        this.conversations = conversations;
        this.messages = messages;
        this.eligibility = eligibility;
        this.users = users;
        this.teachers = teachers;
        this.students = students;
    }

    @org.springframework.transaction.annotation.Transactional
    public OpenConversationResponse openConversation(String userId, String peerEntityId, String peerKind) {
        ChatEligibilityService.ResolvedChatParticipants p = eligibility.resolveForOpenChat(userId, peerEntityId, peerKind);
        Instant now = Instant.now();
        ChatConversationDocument conv;
        if (p.studentPeerChat()) {
            conv = conversations
                    .findByStudentPeerLowRecordIdAndStudentPeerHighRecordIdAndStudentPeerChatIsTrue(
                            p.studentPeerLowRecordId(),
                            p.studentPeerHighRecordId())
                    .orElseGet(() -> conversations.save(ChatConversationDocument.builder()
                            .id(UUID.randomUUID().toString())
                            .schoolId(p.schoolId())
                            .studentPeerChat(true)
                            .studentPeerLowRecordId(p.studentPeerLowRecordId())
                            .studentPeerHighRecordId(p.studentPeerHighRecordId())
                            .studentPeerLowUserId(p.studentPeerLowUserId())
                            .studentPeerHighUserId(p.studentPeerHighUserId())
                            .createdAt(now)
                            .lastMessageAt(now)
                            .lastMessagePreview("")
                            .build()));
        } else {
            conv = conversations
                    .findByTeacherRecordIdAndStudentRecordId(p.teacherRecordId(), p.studentRecordId())
                    .orElseGet(() -> conversations.save(ChatConversationDocument.builder()
                            .id(UUID.randomUUID().toString())
                            .schoolId(p.schoolId())
                            .teacherRecordId(p.teacherRecordId())
                            .studentRecordId(p.studentRecordId())
                            .teacherUserId(p.teacherUserId())
                            .studentUserId(p.studentUserId())
                            .createdAt(now)
                            .lastMessageAt(now)
                            .lastMessagePreview("")
                            .build()));
        }
        return new OpenConversationResponse(conv.getId());
    }

    public List<ChatConversationSummaryResponse> listConversations(String userId) {
        UserEntity me = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserRole role = me.getRole();
        if (role != UserRole.TEACHER && role != UserRole.STUDENT) {
            return List.of();
        }

        List<ChatConversationDocument> rows = new ArrayList<>(conversations.findByParticipantUserId(userId));
        rows.sort(Comparator.comparing(ChatConversationDocument::getLastMessageAt).reversed());

        List<ChatConversationSummaryResponse> out = new ArrayList<>(rows.size());
        for (ChatConversationDocument c : rows) {
            out.add(toSummary(userId, c));
        }
        return out;
    }

    private ChatConversationSummaryResponse toSummary(String currentUserId, ChatConversationDocument c) {
        if (Boolean.TRUE.equals(c.getStudentPeerChat())) {
            boolean iAmLow = currentUserId.equals(c.getStudentPeerLowUserId());
            String peerEntityId = iAmLow ? c.getStudentPeerHighRecordId() : c.getStudentPeerLowRecordId();
            String peerName = students.findById(peerEntityId).map(StudentJpaEntity::getFullName).orElse("Unknown");
            int unread = unreadCountForViewer(currentUserId, c);
            return new ChatConversationSummaryResponse(
                    c.getId(),
                    peerEntityId,
                    "student",
                    peerName,
                    c.getLastMessagePreview() != null ? c.getLastMessagePreview() : "",
                    c.getLastMessageAt(),
                    unread);
        }

        boolean iAmTeacher = currentUserId.equals(c.getTeacherUserId());
        String peerKind = iAmTeacher ? "student" : "teacher";
        String peerEntityId = iAmTeacher ? c.getStudentRecordId() : c.getTeacherRecordId();
        String peerName = iAmTeacher
                ? students.findById(c.getStudentRecordId()).map(StudentJpaEntity::getFullName).orElse("Unknown")
                : teachers.findById(c.getTeacherRecordId()).map(this::formatTeacherName).orElse("Unknown");

        int unread = unreadCountForViewer(currentUserId, c);
        return new ChatConversationSummaryResponse(
                c.getId(),
                peerEntityId,
                peerKind,
                peerName,
                c.getLastMessagePreview() != null ? c.getLastMessagePreview() : "",
                c.getLastMessageAt(),
                unread);
    }

    private int unreadCountForViewer(String viewerUserId, ChatConversationDocument c) {
        Instant lastRead;
        if (Boolean.TRUE.equals(c.getStudentPeerChat())) {
            lastRead = viewerUserId.equals(c.getStudentPeerLowUserId())
                    ? c.getTeacherLastReadAt()
                    : c.getStudentLastReadAt();
        } else {
            lastRead = viewerUserId.equals(c.getTeacherUserId())
                    ? c.getTeacherLastReadAt()
                    : c.getStudentLastReadAt();
        }
        Instant after = lastRead != null ? lastRead : Instant.EPOCH;
        long n = messages.countUnreadFromOthers(c.getId(), viewerUserId, after);
        return n > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) n;
    }

    private String formatTeacherName(TeacherEntity t) {
        UserEntity u = t.getUser();
        if (u == null) {
            return "Teacher";
        }
        return (u.getFirstName() + " " + u.getLastName()).trim();
    }

    public List<ChatMessageResponse> listMessages(
            String userId,
            String conversationId,
            Instant before,
            int limit) {
        if (limit < 1 || limit > 100) {
            limit = 50;
        }
        ChatConversationDocument conv = conversations.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        eligibility.ensureParticipantUser(userId, eligibility.participantsFromConversation(conv));

        var page = PageRequest.of(0, limit);
        List<ChatMessageDocument> batch = before != null
                ? messages.findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(conversationId, before, page)
                : messages.findByConversationIdOrderByCreatedAtDesc(conversationId, page);

        List<ChatMessageResponse> out = new ArrayList<>(batch.size());
        for (int i = batch.size() - 1; i >= 0; i--) {
            ChatMessageDocument m = batch.get(i);
            out.add(new ChatMessageResponse(m.getId(), m.getSenderUserId(), m.getBody(), m.getCreatedAt()));
        }
        return out;
    }

    @org.springframework.transaction.annotation.Transactional
    public ChatMessageResponse sendMessage(String userId, String conversationId, String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message body required");
        }
        String body = rawBody.trim();
        if (body.length() > 8000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message too long");
        }

        ChatConversationDocument conv = conversations.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        ChatEligibilityService.ResolvedChatParticipants p = eligibility.participantsFromConversation(conv);
        eligibility.ensureParticipantUser(userId, p);

        Instant now = Instant.now();
        ChatMessageDocument saved = messages.save(ChatMessageDocument.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId)
                .senderUserId(userId)
                .body(body)
                .createdAt(now)
                .build());

        conv.setLastMessageAt(now);
        conv.setLastMessagePreview(preview(body));
        conversations.save(conv);

        return new ChatMessageResponse(saved.getId(), saved.getSenderUserId(), saved.getBody(), saved.getCreatedAt());
    }

    @org.springframework.transaction.annotation.Transactional
    public void markConversationRead(String userId, String conversationId) {
        ChatConversationDocument conv = conversations.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        ChatEligibilityService.ResolvedChatParticipants p = eligibility.participantsFromConversation(conv);
        eligibility.ensureParticipantUser(userId, p);
        Instant now = Instant.now();
        if (Boolean.TRUE.equals(conv.getStudentPeerChat())) {
            if (userId.equals(conv.getStudentPeerLowUserId())) {
                conv.setTeacherLastReadAt(now);
            } else {
                conv.setStudentLastReadAt(now);
            }
        } else if (userId.equals(conv.getTeacherUserId())) {
            conv.setTeacherLastReadAt(now);
        } else {
            conv.setStudentLastReadAt(now);
        }
        conversations.save(conv);
    }

    private static String preview(String body) {
        if (body.length() <= PREVIEW_MAX) {
            return body;
        }
        return body.substring(0, PREVIEW_MAX) + "…";
    }
}
