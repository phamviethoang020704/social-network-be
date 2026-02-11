package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Response.GroupRes.ListMember;
import com.example.mangxahoi.DTO.Response.GroupRes.MemberInfo;
import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.Service.GroupMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-member")
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<Void> addMember(
            @PathVariable Long groupId,
            @RequestBody List<Long> invitedUserIds,
            Authentication authentication) {
        groupMemberService.inviteMember(invitedUserIds, groupId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}")
    public ListMember getAllMember(@PathVariable Long groupId) {
        return groupMemberService.getAllGroupMembers(groupId);
    }

    @GetMapping("/{groupId}/images")
    public List<ImageResponse> getImageByGroup(@PathVariable Long groupId) {
        return groupMemberService.getImagesByGroupId(groupId);
    }

    //lấy ra danh sách người dùng xin vào nhóm
    @GetMapping("/{groupId}/list-member-request")
    public List<MemberInfo> getListMemberRequest(@PathVariable Long groupId) {
        return groupMemberService.listMemberRequest(groupId);
    }

    //duyet nguoi dung xin vao nhom
    @PostMapping("/{groupId}/approve-request")
    public ResponseEntity<Void> approveMemberRequest(@PathVariable Long groupId, @RequestBody Long applyingId,Authentication authentication) {
        groupMemberService.approveRequestToGroup(groupId,authentication.getName(),applyingId);
        return ResponseEntity.ok().build();
    }

    //xoa nguoi dung xin vao nhom
    @DeleteMapping("/{groupId}/delete-request")
    public ResponseEntity<Void> deleteMemberRequest(@PathVariable Long groupId, @RequestBody Long applyingId,Authentication authentication) {
        groupMemberService.deleteRequestToGroup(groupId,authentication.getName(),applyingId);
        return ResponseEntity.noContent().build();
    }

    //thay doi quyen giua admin,member
    @PatchMapping("/{groupId}/members/{permissionId}/role")
    public MemberInfo changePermissions(
            @PathVariable Long groupId,
            @PathVariable Long permissionId,
            Authentication authentication
            ) {
        return groupMemberService.changePermissions(groupId,authentication.getName(),permissionId);
    }

    //thay doi require join approve
    @PatchMapping("/{groupId}/join-approval")
    public boolean changeJoinApprove(
            @PathVariable Long groupId,
            Authentication auth
    ){
        return groupMemberService.toggleChangeApprove(groupId,auth.getName());
    }
}
