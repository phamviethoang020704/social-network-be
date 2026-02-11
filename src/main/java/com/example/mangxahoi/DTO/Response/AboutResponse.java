package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Enums.GenderUser;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AboutResponse(
        //16
        //Overview
        String university,
        String high_school,
        String work,

        Integer ward_code,
        String ward_name,

        Integer district_code,
        String district_name,

        Integer province_code,
        String province_name,

        //contact
        LocalDate birthday,
        String phoneNumber,
        GenderUser genderUser,
        String social_link,
        LocalDateTime createAt,

        //detail about
        String introduce,
        String biography

) {
}
