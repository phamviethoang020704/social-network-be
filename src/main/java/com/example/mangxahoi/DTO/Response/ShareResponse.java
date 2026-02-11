package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.DTO.ReactionCountDTO;
import com.example.mangxahoi.Enums.ReactionType;
import com.example.mangxahoi.Enums.ShareType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record ShareResponse(
        Long shareId,
        String caption,
        ShareType shareType,
        Long targetId,
        LocalDateTime updatedAt,

        List<ImageResponse> imagesByPost,
        String contentPost,
        String imageByImage,

        //thông tin về người đăng bài mà người dùng chia sẻ
        Long postId,
        Long idPoster,
        String avatarPoster,
        String fullNamePoster,
        LocalDateTime updateAtPoster,

        boolean isLiked,
        ReactionType reactionType,
        Long likeCount,
        Long commentCount,
        List<ReactionCountDTO> reactions,
        //thông tin nguoi chia sẻ
        Long userId,
        String fullName,
        String avatar
) {
}
