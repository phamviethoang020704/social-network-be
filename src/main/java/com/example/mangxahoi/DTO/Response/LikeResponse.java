package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Enums.LikeResult;
import com.example.mangxahoi.Enums.ReactionType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LikeResponse {
    private boolean liked;
    private ReactionType reactionType;
    private long likeCount;
}