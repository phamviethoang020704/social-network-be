package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Enums.CommentTargetType;

public record DeleteCommentResponse(
        Long deletedId,
        Long parentId,
        Long targetId,
        CommentTargetType commentTargetType,
        Long deleteCount,
        Long countCommentRoot
) {
}
