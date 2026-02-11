package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.DTO.ReactionCountDTO;
import com.example.mangxahoi.Enums.ReactionType;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String content,
        String imageUrl,
        String avatar,
        String fullName,
        Long countCommentRoot,
        Long likeCount,
        LocalDateTime updateAt,
        Long countReply,
        Long parentId,
        boolean isLiked,
        ReactionType reactionType,
        List<ReactionCountDTO> reactions,
        String nameUserReceivedReply,
        Long targetId,
        Long userCommentId,
        Long userLoginId
        ) {
}
