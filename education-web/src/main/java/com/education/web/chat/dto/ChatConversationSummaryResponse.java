package com.education.web.chat.dto;

import java.time.Instant;

public record ChatConversationSummaryResponse(
        String id,
        String peerEntityId,
        String peerKind,
        String peerDisplayName,
        String lastMessagePreview,
        Instant lastMessageAt
) {
}
