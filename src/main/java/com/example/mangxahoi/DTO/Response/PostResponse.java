package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.DTO.ReactionCountDTO;
import com.example.mangxahoi.Enums.PostType;
import com.example.mangxahoi.Enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PostResponse {
    private Long id;
    String content;
    PostType postType;

    private List<ImageResponse> images;
    private LocalDateTime updatedAt;
    private List<TagResponse> tags;

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
