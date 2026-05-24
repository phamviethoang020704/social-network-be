package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Enums.NotificationTargetType;
import com.example.mangxahoi.Enums.NotificationType;
import com.example.mangxahoi.Enums.ReactionType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long receiverId,
        Long actorId,
        String actorFullName,
        String actorAvatar,
        NotificationType notificationType,
        NotificationTargetType targetType,
        Long targetId,
        Long friendId,
        String message,
        String redirectUrl,
        boolean read,
        boolean seen,
        LocalDateTime createdAt,
        ReactionType reactionType,

        Long postId,
        Long shareId,
        Long imageId,
        Long commentId,
        Long parentCommentId
) {
}