package com.example.mangxahoi.DTO.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostRequest {
    Long postId;
    Long groupId;
    String content;
    private List<Long> taggedUserIds;
}
