package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Request.LikeRequest;
import com.example.mangxahoi.DTO.Response.LikeResponse;
import com.example.mangxahoi.Service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;
    @PostMapping
    public LikeResponse toggleLike(Authentication authentication, @RequestBody LikeRequest request) {
        return likeService.ToggleLike(
                authentication.getName(),
                request.getTargetId(),
                request.getTargetType(),
                request.getReaction()
        );
    }

}
