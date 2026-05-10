package com.education.web.chat.repository;

import com.education.web.chat.document.ChatMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    List<ChatMessageDocument> findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String conversationId,
            Instant before,
            Pageable pageable);

    List<ChatMessageDocument> findByConversationIdOrderByCreatedAtDesc(
            String conversationId,
            Pageable pageable);

    @Query("{ 'conversationId': ?0, 'senderUserId': { $ne: ?1 }, 'createdAt': { $gt: ?2 } }")
    long countUnreadFromOthers(String conversationId, String viewerUserId, Instant readUpToExclusive);
}
