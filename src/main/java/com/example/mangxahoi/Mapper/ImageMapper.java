package com.example.mangxahoi.Mapper;

import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.Entity.ImageEntity;
import com.example.mangxahoi.Service.ImageService;
import org.springframework.stereotype.Component;
import java.util.List;
import java.awt.*;

@Component
public class ImageMapper {
    private final ImageService imageService;

    public ImageMapper(ImageService imageService) {
        this.imageService = imageService;
    }

    public ImageResponse toResponse(ImageEntity image) {
        return new ImageResponse(image.getId(), imageService.buildImageUrl(image.getImageUrl()));
    }
    public List<ImageResponse> toResponseList(List<ImageEntity> list) {
        return list.stream()
                .map(this::toResponse)
                .toList();
    }
}
