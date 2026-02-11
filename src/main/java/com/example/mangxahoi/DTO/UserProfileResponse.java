package com.example.mangxahoi.DTO;

import com.example.mangxahoi.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    Long countFriend;
    String fullName;
    String avatar;
    String coverPhoto;
}