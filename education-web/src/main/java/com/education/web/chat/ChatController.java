package com.education.web.chat;

import com.education.web.chat.dto.ChatConversationSummaryResponse;
import com.education.web.chat.dto.ChatMessageResponse;
import com.education.web.chat.dto.OpenConversationResponse;
import com.education.web.chat.dto.SendChatMessageRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Validated
@ConditionalOnProperty(name = "education.chat.mongodb-enabled", havingValue = "true")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Ідемпотентне відкриття діалогу (створює запис розмови в Mongo, якщо ще немає).
     * Параметри такі ж, як у сайдбарі чату: {@code peer} = id запису вчителя або учня в MySQL.
     */
    @GetMapping("/open")
    public OpenConversationResponse open(
            @RequestParam("userId") @NotBlank String userId,
            @RequestParam("peerId") @NotBlank String peerId,
            @RequestParam("peerKind") @NotBlank String peerKind) {
        return chatService.openConversation(userId, peerId, peerKind);
    }

    @GetMapping("/conversations")
    public List<ChatConversationSummaryResponse> conversations(@RequestParam("userId") @NotBlank String userId) {
        return chatService.listConversations(userId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageResponse> messages(
            @RequestParam("userId") @NotBlank String userId,
            @PathVariable("conversationId") String conversationId,
            @RequestParam(value = "before", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
            @RequestParam(value = "limit", required = false, defaultValue = "50") @Min(1) @Max(100) int limit) {
        return chatService.listMessages(userId, conversationId, before, limit);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse postMessage(
            @RequestParam("userId") @NotBlank String userId,
            @PathVariable("conversationId") String conversationId,
            @Valid @RequestBody SendChatMessageRequest request) {
        return chatService.sendMessage(userId, conversationId, request.body());
    }
}
