package com.example.mangxahoi.DTO.Chat;

public record TypingEventResponse(
        Long fromUserId,
        boolean typing
) {
}
