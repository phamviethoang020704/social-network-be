package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.DTO.ReactionCountDTO;
import com.example.mangxahoi.Enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ImageViewResponse {
    private ImageResponse current;
    private ImageResponse prev;
    private ImageResponse next;

    LocalDateTime updatedAt;

    private String avatar;
    private String fullName;
    private Long userId;

    private boolean isLiked;
    private ReactionType reactionType;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private List<ReactionCountDTO> reactions = new ArrayList<>();


}