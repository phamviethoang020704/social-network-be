package com.example.mangxahoi.DTO.Request;

import com.example.mangxahoi.Enums.LikeTargetType;
import com.example.mangxahoi.Enums.ReactionType;
import lombok.Getter;

@Getter
public class LikeRequest {
    private Long targetId;
    private LikeTargetType targetType;
    private ReactionType reaction;
}
