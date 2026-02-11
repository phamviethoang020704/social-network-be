package com.example.mangxahoi.DTO.InfoUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvatarUser {
    Long userId;
    private String avatar;
    private String fullName;
}
