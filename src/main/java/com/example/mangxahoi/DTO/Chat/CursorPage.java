package com.example.mangxahoi.DTO.Chat;

import java.util.List;

public record CursorPage<T>(
        List<T> items,
        Long nextCursor,
        boolean hasMore
) {
}
