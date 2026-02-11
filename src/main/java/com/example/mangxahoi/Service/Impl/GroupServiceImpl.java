package com.example.mangxahoi.Service.Impl;

import com.example.mangxahoi.DTO.Request.GroupRequest;
import com.example.mangxahoi.DTO.Response.GroupRes.ListGroupRes;
import com.example.mangxahoi.DTO.Response.GroupResponse;
import com.example.mangxahoi.Entity.*;
import com.example.mangxahoi.Enums.*;
import com.example.mangxahoi.Mapper.GroupMapper;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Service.GroupService;
import com.example.mangxahoi.Service.ImageService;
import com.example.mangxahoi.Service.Search.UpsertService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@Configuration
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PostRepository postRepository;
    private final FriendRepository friendRepository;
    private final GroupMapper groupMapper;
    private final ImageService imageService;
    private final UpsertService searchService;
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional
    public GroupResponse createGroup(String username, GroupRequest request, MultipartFile coverPhoto) throws IOException {
        UserEntity creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        //tạo group
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setGroupName(request.getGroupName());
        groupEntity.setDescription(request.getDescription());
        groupEntity.setPublic(request.isPublicGroup());
        groupEntity.setUserEntity(creator);

        if(coverPhoto!=null && !coverPhoto.isEmpty()){
            String folder = uploadDir + "/group/";
            Files.createDirectories(Paths.get(folder));
            String fileName = System.currentTimeMillis() + "_" + coverPhoto.getOriginalFilename();
            Path filePath =  Paths.get(folder, fileName);
            Files.copy(coverPhoto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            groupEntity.setCoverPhoto("/group/" + fileName);
        }
        else {
            groupEntity.setCoverPhoto("/group-default/group-default.png");
        }

        GroupEntity savedGroup = groupRepository.save(groupEntity);


        //Lưu người tạo nhóm
        GroupMemberEntity owner = new GroupMemberEntity();
        owner.setGroupEntity(savedGroup);
        owner.setGroupJoiningStatus(GroupJoiningStatus.ACCEPTED);
        owner.setRoleName(RoleGroup.OWNER);
        owner.setUserEntity(creator);
        groupMemberRepository.save(owner);

        //mời bạn bè vào group
        if(request.getInvitedUserIds() != null && !request.getInvitedUserIds().isEmpty()){
            List<FriendEntity> acceptedFriends = friendRepository.findAcceptedFriends(creator.getId(),request.getInvitedUserIds());
            for(FriendEntity friend : acceptedFriends){
                UserEntity invitedUser = friend.getUserSend().getId().equals(creator.getId())
                        ? friend.getUserAccept()
                        : friend.getUserSend();
                if (groupMemberRepository.existsByGroupEntityIdAndUserEntityId(savedGroup.getId(),invitedUser.getId())){
                    continue;
                }
                GroupMemberEntity groupMember = new GroupMemberEntity();
                groupMember.setGroupEntity(savedGroup);
                groupMember.setUserEntity(invitedUser);
                groupMember.setRoleName(null);
                groupMember.setGroupJoiningStatus(GroupJoiningStatus.PENDING_RESPONSE);
                groupMember.setInvitedBy(creator);
                groupMemberRepository.save(groupMember);
            }
        }
        searchService.upsert(SearchType.GROUP,savedGroup.getId(), savedGroup.getGroupName());
        return groupMapper.toResponse(savedGroup,creator.getId());
    }

    @Override
    public GroupResponse getGroupById(Long groupId,String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        GroupEntity groupEntity = groupRepository.findById(groupId).orElseThrow(
                () -> new RuntimeException("Group not found")
        );
        return groupMapper.toResponse(groupEntity,user.getId());
    }

    @Override
    public List<ListGroupRes> getMyGroups(String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return groupRepository.getListGroupByUserId(user.getId()).stream().map(
                g -> new ListGroupRes(
                        imageService.buildImageUrl(g.ownerAvatar()),

                        g.groupName(),
                        g.isPublic(),
                        g.groupId(),
                        imageService.buildImageUrl(g.coverPhoto())
                )
        ).toList();
    }

}
