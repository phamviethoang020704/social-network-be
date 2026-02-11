package com.example.mangxahoi.DTO.Chat;

public record TypingEventRequest(
        Long toUserId,
        Boolean typing
) {
}
