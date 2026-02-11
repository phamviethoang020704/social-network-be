package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Entity.ImageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class IntroResponse {
    private String introduce;
    private String work;
    private String provinceName;
    private String university;
    //lấy ra max 9 bạn bè
    private List<FriendResponse> friends;
    //lấy ra max 9 img
    private List<ImageResponse> images;

//    private List<PostResponse> posts;
}
