package com.example.mangxahoi.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostSliceResponse {
    private List<PostResponse> items;
    private boolean hasNext;
    private LocalDateTime nextCursorTime;
    private Long nextCursorId;
}