package com.example.mangxahoi.DTO.Response.GroupRes;

import java.util.List;

public record ListMember(
        List<MemberInfo> owner,
        List<MemberInfo> admin,
        List<MemberInfo> member
) {
}
