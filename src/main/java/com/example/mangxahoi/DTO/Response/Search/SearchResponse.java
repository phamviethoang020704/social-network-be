package com.example.mangxahoi.DTO.Response.Search;

import java.util.List;
import java.util.Map;

public record SearchResponse(
        Map<String, Long> counts,
        List<SearchResultItem> items,
        int page,
        int size
) {
}
