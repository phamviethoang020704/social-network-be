package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Response.NotificationResponse;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.UserRepository;
import com.example.mangxahoi.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public List<NotificationResponse> getMyNotifications(Authentication auth) {
        UserEntity me = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("user not found"));

        return notificationService.getMyNotifications(me);
    }

    @GetMapping("/unread-count")
    public long countUnread(Authentication auth) {
        UserEntity me = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("user not found"));

        return notificationService.countUnread(me);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            Authentication auth,
            @PathVariable Long id
    ) {
        UserEntity me = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("user not found"));

        return notificationService.markAsRead(id, me);
    }
}