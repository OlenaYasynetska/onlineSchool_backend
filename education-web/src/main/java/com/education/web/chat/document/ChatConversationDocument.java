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
@CompoundIndex(name = "ix_teacher_student", def = "{'teacherRecordId': 1, 'studentRecordId': 1}")
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

    /** Останній раз «нижній» учень (studentPeerLow) переглядав діалог; для TS — учитель. */
    private Instant teacherLastReadAt;

    /** Останній раз «верхній» учень (studentPeerHigh) переглядав діалог; для TS — учень. */
    private Instant studentLastReadAt;

    /** Діалог між двома учнями (інакше учитель–учень). */
    private Boolean studentPeerChat;

    /** Діалог між двома вчителями тієї ж школи. */
    private Boolean teacherPeerChat;

    /** Упорядкована пара {@code teachers.id}: low.compareTo(high) <= 0. */
    private String teacherPeerLowRecordId;

    private String teacherPeerHighRecordId;

    private String teacherPeerLowUserId;

    private String teacherPeerHighUserId;

    /** Упорядкована пара {@code students.id}: low.compareTo(high) <= 0. */
    private String studentPeerLowRecordId;

    private String studentPeerHighRecordId;

    private String studentPeerLowUserId;

    private String studentPeerHighUserId;
}
