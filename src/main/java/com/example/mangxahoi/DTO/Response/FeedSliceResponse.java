package com.example.mangxahoi.DTO.Response;

import java.time.LocalDateTime;
import java.util.List;

public record FeedSliceResponse(
        List<FeedResponse> items,
        boolean hasNext,
        LocalDateTime nextCursorTime,
        Long nextCursorId
) {}