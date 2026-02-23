package com.example.mangxahoi.DTO.InfoUser;

import java.io.Serializable;

public record UserProfileCache(
        Long id,
        String avatar,
        String fullName,
        String coverPhoto
) {
}
