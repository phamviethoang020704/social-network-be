package com.example.mangxahoi.Authorization;

import com.example.mangxahoi.Entity.GroupEntity;
import com.example.mangxahoi.Enums.GroupJoiningStatus;
import com.example.mangxahoi.Enums.GroupPermission;
import com.example.mangxahoi.Enums.RoleGroup;

import java.util.EnumSet;
import java.util.Map;

public class GroupRolePermissions {
    public static final Map<RoleGroup, EnumSet<GroupPermission>> MAP = Map.of(
            RoleGroup.OWNER, EnumSet.of(
                    GroupPermission.MANAGE_ADMINS,
                    GroupPermission.APPROVE_MEMBERS,
                    GroupPermission.APPROVE_POSTS,
                    GroupPermission.TRANSFER_OWNER,
                    GroupPermission.DELETE_GROUP,
                    GroupPermission.CHANGE_APPROVE_MEMBERS
            ),
            RoleGroup.ADMIN, EnumSet.of(
                    GroupPermission.APPROVE_MEMBERS,
                    GroupPermission.APPROVE_POSTS
            ),
            RoleGroup.MEMBER,EnumSet.noneOf(GroupPermission.class)
    );

    public static boolean can(
            Long actorId,
            GroupEntity group,
            RoleGroup role,
            GroupJoiningStatus status,
            GroupPermission perm
    ){
        boolean isOwner = group.getUserEntity().getId().equals(actorId);
        if(isOwner) role = RoleGroup.OWNER;

        boolean accepted = (isOwner) || (status == GroupJoiningStatus.ACCEPTED);
        if (!accepted) return false;

        //nếu group đang không cần duyệt xin vào, không cần duyệt đăng bài
        if (perm == GroupPermission.APPROVE_MEMBERS && !group.isRequireJoinApproval()) return false;
        if (perm == GroupPermission.APPROVE_POSTS && !group.isRequirePostApproval()) return false;

        return MAP.getOrDefault(role, EnumSet.noneOf(GroupPermission.class)).contains(perm);
    }
}
