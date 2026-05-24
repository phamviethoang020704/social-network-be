package com.example.mangxahoi.DTO.Request;

import lombok.Getter;

public record LoginRequest(
        String username,
        String password
) {

}
