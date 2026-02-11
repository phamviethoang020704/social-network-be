package com.example.mangxahoi.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class FriendResponse {
    Long id;
    private String fullName;
    private String avatar;
}
