package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Request.LoginRequest;
import com.example.mangxahoi.DTO.Request.RegisterRequest;
import com.example.mangxahoi.Entity.RoleEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.GenderUser;
import com.example.mangxahoi.Enums.SearchType;
import com.example.mangxahoi.Repository.ImageRepository;
import com.example.mangxahoi.Repository.PostRepository;
import com.example.mangxahoi.Repository.RoleRepository;
import com.example.mangxahoi.Repository.UserRepository;
import com.example.mangxahoi.Security.CustomUserDetails;
import com.example.mangxahoi.Security.JwtUtil;
import com.example.mangxahoi.Service.Search.UpsertService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final UpsertService searchService;
    @Value("${app.default.avatar.male}")
    private String maleDefaultAvatar;

    @Value("${app.default.avatar.female}")
    private String femaleDefaultAvatar;

    @Value("${app.default.cover-photo}")
    private String coverPhotoDefault;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        RoleEntity roleUser = roleRepository
                .findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        UserEntity user = new UserEntity();
        user.setFullName(request.getFullName());
        user.setGender(request.getGender());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoleEntity(roleUser);

               // ===== AVATAR DEFAULT =====
        GenderUser genderUser = request.getGender();
        switch (genderUser) {
            case FEMALE -> user.setAvatar(femaleDefaultAvatar);
            case MALE -> user.setAvatar(maleDefaultAvatar);
        }
                // ===== COVER PHOTO DEFAULT =====
        user.setCoverPhoto(coverPhotoDefault);
        UserEntity saveUser = userRepository.save(user);
        searchService.upsert(SearchType.USER, saveUser.getId(), saveUser.getFullName());
    }

    public void login(LoginRequest request, HttpServletResponse response) {

        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai username hoặc password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai username hoặc password");
        }

        String token = jwtUtil.generateToken(user);

        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // local = false, deploy https = true
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1h

        response.addCookie(cookie);
    }

    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

}
