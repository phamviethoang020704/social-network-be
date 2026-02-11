package com.example.mangxahoi.DTO.Response.Search;

public record SearchListItem(
        String type,     // USER | GROUP | POST | SHARE
        Long id,         // target_id
        double score,
        Object data      // PostResponse | ShareResponse | ResultSearchUser | ResultSearchGroup
) {}