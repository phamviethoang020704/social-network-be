package com.example.mangxahoi.DTO.Request;

import com.example.mangxahoi.Enums.CommentTargetType;

public record CommentRequest(
         String content,
         CommentTargetType commentTargetType,
         Long targetId,
         Long parentId,
         Long replyId,
         Long commentId
) {

}
