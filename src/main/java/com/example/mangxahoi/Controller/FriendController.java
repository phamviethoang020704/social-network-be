package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Response.FriendResponse;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.FriendStatus;
import com.example.mangxahoi.Repository.UserRepository;
import com.example.mangxahoi.Service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;

    @GetMapping("/status/{userId}")
    public FriendStatus getStatus(Authentication auth, @PathVariable Long userId) {
        UserEntity viewer = userRepository.findByUsername(auth.getName()).orElseThrow();
        UserEntity owner = userRepository.findById(userId).orElseThrow();
        return friendService.getStatus(viewer, owner);
    }

    @PostMapping("/send/{userId}")
    public void send(Authentication auth, @PathVariable Long userId) {
        UserEntity sender = userRepository.findByUsername(auth.getName()).orElseThrow();
        UserEntity receiver = userRepository.findById(userId).orElseThrow();
        friendService.send(sender, receiver);
    }

    @PostMapping("/accept/{userId}")
    public void accept(Authentication auth, @PathVariable Long userId) {
        UserEntity receiver = userRepository.findByUsername(auth.getName()).orElseThrow();
        UserEntity sender = userRepository.findById(userId).orElseThrow();
        friendService.accept(sender, receiver);
    }

    @DeleteMapping("/cancel/{userId}")
    public void cancel(Authentication auth, @PathVariable Long userId) {
        UserEntity u1 = userRepository.findByUsername(auth.getName()).orElseThrow();
        UserEntity u2 = userRepository.findById(userId).orElseThrow();
        friendService.cancel(u1, u2);
    }

    //tìm kiếm user đã kết bạn theo fullName
    @GetMapping("/search")
    public ResponseEntity<List<FriendResponse>> searchFriends(Authentication auth, @RequestParam String keyword) {
        String username = auth.getName();
        return ResponseEntity.ok(
                friendService.searchFriendsByFullName(username, keyword)
        );
    }
}
