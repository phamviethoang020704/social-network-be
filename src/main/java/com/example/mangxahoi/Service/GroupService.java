package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Request.GroupRequest;
import com.example.mangxahoi.DTO.Response.GroupRes.ListGroupRes;
import com.example.mangxahoi.DTO.Response.GroupResponse;
import com.example.mangxahoi.Entity.GroupEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface GroupService {
    GroupResponse createGroup(String username, GroupRequest request, MultipartFile coverPhoto) throws IOException;
    GroupResponse getGroupById(Long groupId, String username);
    List<ListGroupRes> getMyGroups(String username);
}
