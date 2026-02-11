package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Request.GroupRequest;
import com.example.mangxahoi.DTO.Response.GroupRes.ListGroupRes;
import com.example.mangxahoi.DTO.Response.GroupResponse;
import com.example.mangxahoi.Entity.GroupEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Mapper.GroupMapper;
import com.example.mangxahoi.Repository.UserRepository;
import com.example.mangxahoi.Service.GroupMemberService;
import com.example.mangxahoi.Service.GroupService;
import com.example.mangxahoi.Service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final UserService userService;
    private final GroupService groupService;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final GroupMemberService groupMemberService;

    public GroupController(UserService userService, GroupService groupService, UserRepository userRepository, GroupMapper groupMapper, GroupMemberService groupMemberService) {
        this.userService = userService;
        this.groupService = groupService;
        this.userRepository = userRepository;
        this.groupMapper = groupMapper;
        this.groupMemberService = groupMemberService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GroupResponse> createGroup(
            @RequestPart("data")GroupRequest request,
            @RequestPart(value = "coverPhoto",required = false) MultipartFile coverPhoto,
            Authentication authentication
            ) throws IOException {

        return ResponseEntity.ok(groupService.createGroup(authentication.getName(), request, coverPhoto));
    }
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId,Authentication authentication) {
        return ResponseEntity.ok(groupService.getGroupById(groupId, authentication.getName()));
    }

    @PostMapping("/{groupId}/request-to-group")
    public ResponseEntity<Void> requestToGroup(
            @PathVariable Long groupId,
            Authentication authentication
    ){
        groupMemberService.requestToGroup(groupId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/cancel-request")
    public ResponseEntity<Void> cancelRequestToGroup(
            @PathVariable Long groupId,
            Authentication authentication
    ){
        groupMemberService.cancelRequestToGroup(groupId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all-group")
    public List<ListGroupRes> getMyGroups(Authentication authentication){
        return groupService.getMyGroups(authentication.getName());
    }
}
