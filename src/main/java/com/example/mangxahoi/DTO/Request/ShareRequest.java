package com.example.mangxahoi.DTO.Request;

import com.example.mangxahoi.Enums.ShareType;

public record ShareRequest(
        Long targetId,
        ShareType shareType,
        String caption
) {
}
