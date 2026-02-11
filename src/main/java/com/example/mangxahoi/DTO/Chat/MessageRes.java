package com.example.mangxahoi.DTO.Chat;

import java.time.LocalDateTime;

public record MessageRes(
        Long messageId,
        Long conversationId,
        Long fromUserId,
        String text,
        String imageUrl,
        LocalDateTime createdAt
) {
}
