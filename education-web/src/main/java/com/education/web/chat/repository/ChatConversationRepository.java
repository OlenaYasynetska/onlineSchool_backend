package com.education.web.chat.repository;

import com.education.web.chat.document.ChatConversationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends MongoRepository<ChatConversationDocument, String> {

    Optional<ChatConversationDocument> findByTeacherRecordIdAndStudentRecordId(
            String teacherRecordId,
            String studentRecordId);

    Optional<ChatConversationDocument> findByStudentPeerLowRecordIdAndStudentPeerHighRecordIdAndStudentPeerChatIsTrue(
            String studentPeerLowRecordId,
            String studentPeerHighRecordId);

    @Query("{ $or: [ { 'teacherUserId': ?0 }, { 'studentUserId': ?0 }, { 'studentPeerLowUserId': ?0 }, { 'studentPeerHighUserId': ?0 } ] }")
    List<ChatConversationDocument> findByParticipantUserId(String userId);
}
