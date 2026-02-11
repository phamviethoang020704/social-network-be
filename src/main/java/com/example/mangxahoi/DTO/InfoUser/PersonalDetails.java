package com.example.mangxahoi.DTO.InfoUser;

import com.example.mangxahoi.Enums.GenderUser;

import java.time.LocalDate;

public record PersonalDetails(
        LocalDate birthday,
        GenderUser genderUser,
        String phoneNumber
) {
}
