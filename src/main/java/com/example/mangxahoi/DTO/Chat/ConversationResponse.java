package com.example.mangxahoi.DTO.Chat;

import com.example.mangxahoi.Enums.MessageType;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long recipientId,
        String recipientAvatar,
        String recipientFullName,

        String lastMessage,
        Long lastSenderId,
        LocalDateTime lastMessageAt,
        MessageType lastMessageType
) {
}
