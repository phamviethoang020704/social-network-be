package com.example.mangxahoi.Controller.User;

import com.example.mangxahoi.DTO.InfoUser.*;
import com.example.mangxahoi.DTO.Response.*;
import com.example.mangxahoi.DTO.UserMeDTO;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.UserRepository;
import com.example.mangxahoi.Service.Cache.UserProfileCacheService;
import com.example.mangxahoi.Service.FeedItemService;
import com.example.mangxahoi.Service.Impl.UserServiceImpl;
import com.example.mangxahoi.Service.IntroService;
import com.example.mangxahoi.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final IntroService introService;
    private final FeedItemService feedItemService;
    private final UserProfileCacheService userProfileCacheService;

    @GetMapping("/me")
    public ResponseEntity<UserMeDTO> me(Authentication authentication) {

        UserEntity user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                new UserMeDTO(user.getId(), user.getUsername(), user.getAvatarPostId(), user.getCoverPostId())
        );
    }
    //lấy ra id user - logc kết bạn
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
    //lấy ra avatar trả ra header
    @GetMapping("/avt")
    public ResponseEntity<AvatarUser> getMyAvatar(HttpServletRequest request){
        return ResponseEntity.ok(userService.getAvatar(request));
    }

    //lấy introduce của theo userId
    @GetMapping("/intro")
    public ResponseEntity<IntroResponse> intro(@RequestParam Long userId){
        return ResponseEntity.ok(introService.buildIntro(userId));
    }
    //lấy ra tất cả bài của user
    @GetMapping("/feed-by-id")
    public ResponseEntity<FeedSliceResponse> feed(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId
    ) {
        return ResponseEntity.ok(introService.feedUserById(userId, size, cursorTime, cursorId));
    }

    //ấy ra tất cả bài của tất cả user
    @GetMapping("/feeds")
    public ResponseEntity<FeedSliceResponse> feeds(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId
    ){
        return ResponseEntity.ok(feedItemService.allFeed(size, cursorTime, cursorId));
    }
    //Lấy ra tất cả thông tin user
    @GetMapping("/about/{userId}")
    public ResponseEntity<AboutResponse> about(@PathVariable Long userId) {
        return ResponseEntity.ok(introService.getAbout(userId));
    }

    //lấy ra tất cả bạn của user
    @GetMapping("/{userId}/friends")
    public List<FriendResponse> getAllFriends(
            @PathVariable Long userId,
            @RequestParam int page
    ) {
     return introService.getAllFriends(userId, page);
    }

    //lấy ra tất cả bạn chung
    @GetMapping("/{userId}/mutual-friends")
    public ResponseEntity<List<FriendResponse>> getMutualFriends(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                introService.getMutualFriends(userId, authentication.getName())
        );
    }
    //lấy ra hết ảnh user
    @GetMapping("/{userId}/images")
    public ResponseEntity<List<ImageResponse>> getAllImages(@PathVariable Long userId) {
        return ResponseEntity.ok(
                introService.getAllImagesByUser(userId)
        );
    }

    //lấy ra avatar,fullName,coverPhoto
    @GetMapping("/{userId}/basic-information")
    public UserProfileCache getUserProfileCache(@PathVariable Long userId) {
        return userProfileCacheService.getProfile(userId);
    }

    //thay đổi ảnh đại diện, ảnh bìa
    @PostMapping(value = "/change-image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FeedResponse changeImage(
            @RequestPart MultipartFile image,
            @RequestPart ChangeImage request,
            Authentication authentication
    ) throws IOException
    {
        return userService.changImageUser(request, image, authentication.getName());
    }

    //sửa intro,bio
    @PostMapping("/edit-intro")
    public ResponseEntity<?> editProfileIntro(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ){
        Map<String, String> result =
                introService.editIntro(body, authentication.getName());
        return ResponseEntity.ok(result);
    }

    //sửa work
    @PostMapping("/edit-work")
    public ResponseEntity<?> editProfileWork(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String work = body.get("work");

        if (work == null) {
            return ResponseEntity.badRequest().body("Missing field: work");
        }

        Map<String, String> result =
                introService.editWork(body, authentication.getName());

        return ResponseEntity.ok(result);
    }

    //sửa education, high-school
    @PostMapping("/edit-education")
    public ResponseEntity<?> editEducation(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                introService.editEducation(body, authentication.getName())
        );
    }

    //sửa birthday gender phone
    @PostMapping("/edit-personal-detail")
    public PersonalDetails editPersonalDetails(
            @RequestBody PersonalDetails data,
            Authentication authentication
    ) {
        return introService.editPersonalDetails(data, authentication.getName()
        );
    }
    // sửa address
    @PostMapping("/edit-address")
    public ResponseEntity<?> updateAddress(
            @RequestBody AddressDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                introService.editAddress(request, authentication.getName())
        );
    }

    //sửa link
    @PatchMapping("/edit-social-link")
    public ResponseEntity<?> editSocialLink(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                introService.editSocialLink(body, authentication.getName())
        );
    }
}
