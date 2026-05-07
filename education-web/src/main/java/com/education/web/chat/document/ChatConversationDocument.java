package com.education.web.chat.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_conversations")
@CompoundIndex(name = "ux_teacher_student", def = "{'teacherRecordId': 1, 'studentRecordId': 1}", unique = true)
public class ChatConversationDocument {

    @Id
    private String id;

    @Indexed
    private String schoolId;

    /** {@code teachers.id} */
    private String teacherRecordId;

    /** {@code students.id} */
    private String studentRecordId;

    private String teacherUserId;
    private String studentUserId;

    private Instant lastMessageAt;

    private String lastMessagePreview;

    private Instant createdAt;
}
