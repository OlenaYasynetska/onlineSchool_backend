package com.education.web.chat.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndex(name = "ix_conversation_created", def = "{'conversationId': 1, 'createdAt': -1}")
public class ChatMessageDocument {

    @Id
    private String id;

    private String conversationId;

    private String senderUserId;

    private String body;

    private Instant createdAt;
}
