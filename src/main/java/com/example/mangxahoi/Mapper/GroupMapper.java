package com.example.mangxahoi.Mapper;

import com.example.mangxahoi.DTO.Response.GroupResponse;
import com.example.mangxahoi.Entity.GroupEntity;
import com.example.mangxahoi.Entity.GroupMemberEntity;
import com.example.mangxahoi.Enums.GroupJoiningStatus;
import com.example.mangxahoi.Enums.RoleGroup;
import com.example.mangxahoi.Repository.GroupMemberRepository;
import com.example.mangxahoi.Service.ImageService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GroupMapper {
    private final GroupMemberRepository groupMemberRepository;
    private final ImageService imageService;

    public GroupMapper(GroupMemberRepository groupMemberRepository, ImageService imageService) {
        this.groupMemberRepository = groupMemberRepository;
        this.imageService = imageService;
    }

    public GroupResponse toResponse(GroupEntity groupEntity, Long userId) {
        Optional<GroupMemberEntity> meOpt =
                groupMemberRepository.findByUserEntityIdAndGroupEntityId(
                        userId, groupEntity.getId()
                );

        GroupJoiningStatus meJoiningStatus =
                meOpt.map(GroupMemberEntity::getGroupJoiningStatus).orElse(null);

        RoleGroup meRoleGroup =
                meOpt.map(GroupMemberEntity::getRoleName).orElse(null);

        return new  GroupResponse(
                groupEntity.getId(),
                groupEntity.getGroupName(),
                groupEntity.getDescription(),
                imageService.buildImageUrl(groupEntity.getCoverPhoto()),
                groupEntity.isPublic(),
                groupEntity.isRequireJoinApproval(),

                groupMemberRepository.countByGroupEntityIdAndGroupJoiningStatus(groupEntity.getId(), GroupJoiningStatus.ACCEPTED),

                groupEntity.getUserEntity().getId(),
                groupEntity.getUserEntity().getFullName(),
                imageService.buildImageUrl(groupEntity.getUserEntity().getAvatar()),

                meJoiningStatus,
                meRoleGroup,
                groupEntity.getCreatedAt()
        );
    }
}
