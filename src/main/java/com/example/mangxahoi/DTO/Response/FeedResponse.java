package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Enums.FeedType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class FeedResponse {
    private Long id;
    private FeedType feedType; // POST | SHARE
    private LocalDateTime updatedAt;
    private PostResponse post;
    private ShareResponse share;
}
