package com.example.mangxahoi.DTO.Response.Search;

public record SearchResultItem(
        String type,
        Long id,
        Double score,

        // USER
        String fullName,
        String avatar,

        // GROUP
        String groupName,
        String coverPhoto,

        // POST/SHARE (hiển thị excerpt)
        String text
) {
}
