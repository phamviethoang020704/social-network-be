package com.example.mangxahoi.DTO.Response;

import com.example.mangxahoi.Entity.ImageEntity;
import com.example.mangxahoi.Service.ImageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String imageUrl;
}
