package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Response.ImageViewResponse;
import com.example.mangxahoi.Service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
@AllArgsConstructor
public class ImageController {
    private final ImageService imageService;
    @GetMapping
    public ImageViewResponse getImage(@RequestParam Long imgId,
                                      @RequestParam Long postId,
                                      Authentication authentication
    ) {
        return imageService.getImageView(imgId, postId, authentication.getName());
    }

}
