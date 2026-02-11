package com.example.mangxahoi.DTO.Response.GroupRes;

public record ListGroupRes(
        String ownerAvatar,

        String groupName,
        boolean isPublic,
        Long groupId,
        String coverPhoto

) {
}
