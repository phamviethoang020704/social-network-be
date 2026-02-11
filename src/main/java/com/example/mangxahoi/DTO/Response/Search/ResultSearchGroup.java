package com.example.mangxahoi.DTO.Response.Search;

import com.example.mangxahoi.Enums.GroupJoiningStatus;

public record ResultSearchGroup(
        Long id,
        String coverPhoto,
        String groupName,
        GroupJoiningStatus status
) {
}
