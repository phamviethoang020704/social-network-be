package com.example.mangxahoi.DTO.Request;

import lombok.Getter;

import java.util.List;

@Getter
public class GroupRequest {
    private String groupName;
    private String description;
    private boolean publicGroup;
    private List<Long> invitedUserIds;
}
