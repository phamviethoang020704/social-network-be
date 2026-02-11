package com.example.mangxahoi.DTO.Chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long conversationId,

        Long fromUserId,
        Long toUserId,

        String text,
        String imageUrl,

        LocalDateTime createdAt
) {
}
