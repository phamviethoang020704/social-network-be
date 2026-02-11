package com.example.mangxahoi.DTO;

import com.example.mangxahoi.Enums.PostType;

public record EditContent(
        Long id,
        String content,

        PostType postType
) {
}
