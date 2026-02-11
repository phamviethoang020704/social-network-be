package com.example.mangxahoi.Service;

import com.example.mangxahoi.Authorization.GroupRolePermissions;
import com.example.mangxahoi.DTO.Response.GroupRes.ListMember;
import com.example.mangxahoi.DTO.Response.GroupRes.MemberInfo;
import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.Entity.FriendEntity;
import com.example.mangxahoi.Entity.GroupEntity;
import com.example.mangxahoi.Entity.GroupMemberEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.GroupJoiningStatus;
import com.example.mangxahoi.Enums.GroupPermission;
import com.example.mangxahoi.Enums.RoleGroup;
import com.example.mangxahoi.Enums.TypeJoinGroup;
import com.example.mangxahoi.Repository.FriendRepository;
import com.example.mangxahoi.Repository.GroupMemberRepository;
import com.example.mangxahoi.Repository.GroupRepository;
import com.example.mangxahoi.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.mangxahoi.Authorization.GroupRolePermissions;

import javax.management.relation.Role;
import java.util.List;
import java.util.Optional;

@Service
public class GroupMemberService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final ImageService imageService;
    public GroupMemberService(FriendRepository friendRepository, UserRepository userRepository, GroupMemberRepository groupMemberRepository, GroupRepository groupRepository, ImageService imageService) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.imageService = imageService;
    }

    // moi member
    public void inviteMember(List<Long> invitedUserIds,Long groupId, String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));

        //kiểm tra xem người mời đã ở trong nhóm chưa
        if(!groupMemberRepository.existsByUserEntityIdAndGroupEntityIdAndGroupJoiningStatus(user.getId(),group.getId(),GroupJoiningStatus.ACCEPTED)){
            throw new RuntimeException("người dùng chưa vào nhóm không có quyền mời");
        }


        if(invitedUserIds != null && !invitedUserIds.isEmpty()){
            List<FriendEntity> acceptedFriends = friendRepository.findAcceptedFriends(user.getId(),invitedUserIds);
            for(FriendEntity friend : acceptedFriends){
                UserEntity invitedUser = friend.getUserSend().getId().equals(user.getId())
                        ? friend.getUserAccept()
                        : friend.getUserSend();
                if (groupMemberRepository.existsByGroupEntityIdAndUserEntityId(group.getId(),invitedUser.getId())){
                    continue;
                }
                GroupMemberEntity groupMember = new GroupMemberEntity();
                groupMember.setGroupEntity(group);
                groupMember.setUserEntity(invitedUser);
                groupMember.setRoleName(null);
                groupMember.setGroupJoiningStatus(GroupJoiningStatus.PENDING_RESPONSE);
                groupMember.setInvitedBy(user);
                groupMemberRepository.save(groupMember);
            }
        }
    }

    //lấy ra all member
    public ListMember getAllGroupMembers(Long groupId){
        groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        return new ListMember(
                mapAvatar(groupMemberRepository.findAllGroupMembers(groupId, RoleGroup.OWNER)),
                mapAvatar(groupMemberRepository.findAllGroupMembers(groupId, RoleGroup.ADMIN)),
                mapAvatar(groupMemberRepository.findAllGroupMembers(groupId, RoleGroup.MEMBER))
        );
    }
    private List<MemberInfo> mapAvatar(List<MemberInfo> members) {
        return members.stream()
                .map(m -> new MemberInfo(
                        m.id(),
                        m.fullName(),
                        imageService.buildImageUrl(m.avatar()),
                        m.roleGroup()
                ))
                .toList();
    }

    //lay ra all image trong group
    public List<ImageResponse> getImagesByGroupId(Long groupId){
        groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        return mapImage(groupMemberRepository.getImageByGroupId(groupId));
    }
    private List<ImageResponse> mapImage(List<ImageResponse> images){
        return images.stream().map(i -> new ImageResponse(
                i.getId(),
                imageService.buildImageUrl(i.getImageUrl())
        ))
                .toList();
    }

    //xin vào nhóm hoặc chấp nhận lời mời vào nhóm
    @Transactional
    public void requestToGroup(Long groupId,String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));

        Optional<GroupMemberEntity> existMeInGroup = groupMemberRepository.findByUserEntityIdAndGroupEntityId(user.getId(),group.getId());

        existMeInGroup.ifPresentOrElse(
                //đã có bản ghi chỉ cập nhật nếu PENDING_RESPONSE
                //chuyển từ được mời thành yêu cầu vào nhóm
                exist -> {
                    if (group.isRequireJoinApproval()){
                        if (exist.getGroupJoiningStatus() != GroupJoiningStatus.PENDING_RESPONSE) {
                            throw new IllegalStateException("nguoi dung đã gưỉ lời mời vào nhóm hoặc đã ở trong nhóm");
                        }
                        exist.setGroupJoiningStatus(GroupJoiningStatus.PENDING_APPROVAL);
                    }
                    else {
                        exist.setGroupJoiningStatus(GroupJoiningStatus.ACCEPTED);
                        exist.setRoleName(RoleGroup.MEMBER);
                    }
                },
                () -> {
                    //chưa có bản ghi
                    GroupMemberEntity newMember = new GroupMemberEntity();
                    if (group.isRequireJoinApproval()){
                        newMember.setGroupJoiningStatus(GroupJoiningStatus.PENDING_APPROVAL);
                    }
                    else {
                        newMember.setGroupJoiningStatus(GroupJoiningStatus.ACCEPTED);
                        newMember.setRoleName(RoleGroup.MEMBER);
                    }
                    newMember.setUserEntity(user);
                    newMember.setGroupEntity(group);

                    groupMemberRepository.save(newMember);
                }
        );
    }

    //hủy yu cầu vào nhóm hoặc rời khỏi nhóm
    @Transactional
    public void cancelRequestToGroup(Long groupId,String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        GroupMemberEntity member
                = groupMemberRepository
                .findByUserEntityIdAndGroupEntityId(user.getId(),group.getId()).orElseThrow(() -> new RuntimeException("nguoi dung chua gui yeu cau vao nhom"));

        GroupJoiningStatus status = member.getGroupJoiningStatus();
        if (status == GroupJoiningStatus.PENDING_APPROVAL
                || status == GroupJoiningStatus.ACCEPTED) {

            groupMemberRepository.delete(member);
            return;
        }
        throw new RuntimeException("không thể hủy trong trạng thái: " + status);
    }

    //lấy ra danh sách yêu cầu vào nhóm
    public List<MemberInfo> listMemberRequest(Long groupId){
        groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        return mapAvatar(groupMemberRepository.getListMemberRequest(groupId,GroupJoiningStatus.PENDING_APPROVAL));
    }

    //duyệt thành viên vào nhóm
    @Transactional
    public void approveRequestToGroup(
            Long groupId,
            String username,
            Long applyingId
    ){
        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        GroupMemberEntity member = groupMemberRepository.findByUserEntityIdAndGroupEntityId(user.getId(),group.getId()).orElse(null);
        if (!GroupRolePermissions.can(user.getId(),group,member.getRoleName(),member.getGroupJoiningStatus(),GroupPermission.APPROVE_MEMBERS)){
            throw new AccessDeniedException("Không đủ quyền");
        }
        UserEntity applyUser = userRepository.findById(applyingId).orElseThrow(() -> new RuntimeException("user not found"));
        GroupMemberEntity applyMember = groupMemberRepository.findByUserEntityIdAndGroupEntityId(applyUser.getId(),group.getId()).orElse(null);
        applyMember.setGroupJoiningStatus(GroupJoiningStatus.ACCEPTED);
        applyMember.setRoleName(RoleGroup.MEMBER);
    }

    //xóa yêu cầu vào nhóm
    @Transactional
    public void deleteRequestToGroup(
            Long groupId,
            String username,
            Long applyingId
    ){
        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        GroupMemberEntity member = groupMemberRepository.findByUserEntityIdAndGroupEntityId(user.getId(),group.getId()).orElse(null);
        if (!GroupRolePermissions.can(user.getId(),group,member.getRoleName(),member.getGroupJoiningStatus(),GroupPermission.APPROVE_MEMBERS)){
            throw new AccessDeniedException("Không đủ quyền");
        }
        UserEntity applyUser = userRepository.findById(applyingId).orElseThrow(() -> new RuntimeException("user not found"));
        GroupMemberEntity applyMember = groupMemberRepository.findByUserEntityIdAndGroupEntityId(applyUser.getId(),group.getId()).orElse(null);

        groupMemberRepository.delete(applyMember);
    }

    //thay đổi quyền giua admin va member
    @Transactional
    public MemberInfo changePermissions(Long groupId,String username, Long changedPersonId){
        UserEntity me = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        UserEntity changedPerson = userRepository.findById(changedPersonId).orElseThrow(() -> new RuntimeException("changedPerson not found"));

        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));

        GroupMemberEntity meGroup = groupMemberRepository.findByUserEntityIdAndGroupEntityId(me.getId(),group.getId()).orElseThrow(() -> new RuntimeException("nguoi duyet chua vao nhom"));
        GroupMemberEntity changedPersonGroup = groupMemberRepository.findByUserEntityIdAndGroupEntityId(changedPersonId,group.getId()).orElseThrow(() -> new RuntimeException("nguoi dc duyet chua vao nhom"));

        if (!GroupRolePermissions.can(me.getId(),group,meGroup.getRoleName(),changedPersonGroup.getGroupJoiningStatus(),GroupPermission.MANAGE_ADMINS)){
            throw new AccessDeniedException("Không đủ quyền");
        }

        RoleGroup newRole;
        if (changedPersonGroup.getRoleName() == RoleGroup.ADMIN){
            changedPersonGroup.setRoleName(RoleGroup.MEMBER);
            newRole = RoleGroup.MEMBER;
        }
        else if (changedPersonGroup.getRoleName() == RoleGroup.MEMBER) {
            changedPersonGroup.setRoleName(RoleGroup.ADMIN);
            newRole = RoleGroup.ADMIN;
        }
        else {
            throw new RuntimeException("nguoi duojc thay doi quyen la owner");
        }
        return new MemberInfo(
                changedPerson.getId(),
                changedPerson.getFullName(),
                imageService.buildImageUrl(changedPerson.getAvatar()),
                newRole
        );
    }

    //thay đổi required join approve
    @Transactional
    public boolean toggleChangeApprove(Long groupId, String username){
        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("group not found"));
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        GroupMemberEntity member = groupMemberRepository.findByUserEntityIdAndGroupEntityId(user.getId(),group.getId()).orElseThrow(() -> new RuntimeException("chua vao nhom"));
        if (!GroupRolePermissions.can(user.getId(),group,member.getRoleName(),member.getGroupJoiningStatus(),GroupPermission.CHANGE_APPROVE_MEMBERS)){
            throw new AccessDeniedException("Không đủ quyền");
        }
        boolean newValue = !group.isRequireJoinApproval();
        group.setRequireJoinApproval(newValue);

        if (!newValue) {
            groupMemberRepository
                    .findAllByGroupEntityIdAndGroupJoiningStatus(
                            groupId,
                            GroupJoiningStatus.PENDING_APPROVAL
                    )
                    .forEach(m -> {
                        m.setGroupJoiningStatus(GroupJoiningStatus.ACCEPTED);
                        m.setRoleName(RoleGroup.MEMBER);
                    });
        }
        return newValue;
    }
}
