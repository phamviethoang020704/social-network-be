package com.example.mangxahoi.DTO.InfoUser;

public record AddressDTO(
     Integer provinceCode,

     String provinceName,

     Integer districtCode,

     String districtName,

     Integer wardCode,

     String wardName
) {
}
