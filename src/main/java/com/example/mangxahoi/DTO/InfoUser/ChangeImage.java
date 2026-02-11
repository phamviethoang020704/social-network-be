package com.example.mangxahoi.DTO.InfoUser;

import com.example.mangxahoi.Enums.PostType;

public record ChangeImage(
        String content,
        PostType postType
) {
}
