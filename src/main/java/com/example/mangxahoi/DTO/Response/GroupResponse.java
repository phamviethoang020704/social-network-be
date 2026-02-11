package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Enums.GroupJoiningStatus;
import com.example.mangxahoi.Enums.RoleGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
@AllArgsConstructor
@Getter
public class GroupResponse {
    private Long id;
    private String groupName;
    private String description;
    private String coverPhoto;
    private boolean isPublic;
    private boolean requireJoinApproval;

    private Long countMembers;

    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;

    private GroupJoiningStatus meJoiningStatus;
    private RoleGroup meRoleGroup;

    private LocalDateTime createdAt;
}
