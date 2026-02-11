package com.example.mangxahoi.DTO.Response.Search;

public record SuggestItem(
        String type,        // RECENT | USER | GROUP | POST | SHARE
        Long id,
        String text,
        String action,      // NAVIGATE | SEARCH
        String avatar,
        String coverPhoto,
        String searchText,
        String navigateTo,  // "profile" | "group" (tuỳ FE)
        Double score,
        Long recentId,      // chỉ dùng cho RECENT để xoá
        Boolean canDelete
) {
}
