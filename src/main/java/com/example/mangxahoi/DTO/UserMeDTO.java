package com.example.mangxahoi.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserMeDTO {
    private Long id;
    private String username;

    Long avatarPostId;
    Long coverPostId;
}