package com.example.mangxahoi.DTO.Response.Search;

import java.util.List;
import java.util.Map;

public record SearchListResponse(
        String q,
        String type,                  // ALL | USER | GROUP | POST | SHARE
        Map<String, Long> counts,     // đếm theo type
        int page,
        int size,
        List<SearchListItem> items
) {}