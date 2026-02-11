package com.example.mangxahoi.DTO.Chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatSendRequest(
        @NotNull
        Long toUserId,

        String text,
        String imageUrl
) {
}
