package com.education.web.chat.repository;

import com.education.web.chat.document.ChatMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

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
}
