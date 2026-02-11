package com.example.mangxahoi.DTO.Response.Search;

public record ResultSearchUser(
        Long userId,
        String avatar,
        String fullName,
        Long isAccepted
) {
}
