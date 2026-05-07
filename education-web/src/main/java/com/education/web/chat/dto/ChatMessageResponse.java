package com.education.web.chat.dto;

import java.time.Instant;

public record ChatMessageResponse(String id, String senderUserId, String body, Instant createdAt) {
}
