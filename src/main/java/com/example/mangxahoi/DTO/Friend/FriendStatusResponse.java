package com.example.mangxahoi.DTO.Friend;

import com.example.mangxahoi.Enums.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class FriendStatusResponse {
    private FriendStatus status;
}
