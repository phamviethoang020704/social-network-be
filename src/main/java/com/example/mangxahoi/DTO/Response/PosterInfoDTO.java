package com.example.mangxahoi.DTO.Response;

import java.time.LocalDateTime;

public record PosterInfoDTO(
        Long targetId,

        Long id,
        String fullName,
        String avatar,
        LocalDateTime updatedAt
) {}