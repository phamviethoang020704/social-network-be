package com.example.mangxahoi.DTO.Response.GroupRes;

import com.example.mangxahoi.Enums.RoleGroup;

public record MemberInfo(
        Long id,
        String fullName,
        String avatar,
        RoleGroup roleGroup
) {
}
