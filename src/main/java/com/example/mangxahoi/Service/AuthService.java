package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Request.LoginRequest;
import com.example.mangxahoi.DTO.Request.RegisterRequest;
import com.example.mangxahoi.Entity.RefreshTokenEntity;
import com.example.mangxahoi.Entity.RoleEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.GenderUser;
import com.example.mangxahoi.Enums.SearchType;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Security.CookieUtil;
import com.example.mangxahoi.Security.CustomUserDetails;
import com.example.mangxahoi.Security.JwtUtil;
import com.example.mangxahoi.Service.Cache.UserProfileCacheService;
import com.example.mangxahoi.Service.Search.UpsertService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final UpsertService searchService;
    private final UserProfileCacheService userProfileCacheService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
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

    @Transactional
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

        //put cache
        userProfileCacheService.updateProfile(userProfileCacheService.fromEntity(saveUser));

        searchService.upsert(SearchType.USER, saveUser.getId(), saveUser.getFullName());
    }

    public void login(LoginRequest res, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        res.username(), res.password()
                )
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        String accessToken = jwtUtil.generateToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        // lưu refresh token để revoke/rotate được
        saveRefreshToken(username, refreshToken);

        CookieUtil.addAccessCookie(response, accessToken);
        CookieUtil.addRefreshCookie(response, refreshToken);
    }

    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request, "refresh_token");
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
        }

        CookieUtil.clearCookie(response, "access_token", "/");
        CookieUtil.clearCookie(response, "refresh_token", "/auth/refresh");
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request, "refresh_token");
        if (refreshToken == null) {
            throw new RuntimeException("Missing refresh token");
        }

        // 1) JWT hợp lệ + đúng loại refresh
        if (!jwtUtil.validateTokenType(refreshToken, "refresh_token")) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2) Check tồn tại trong DB và chưa revoked, chưa hết hạn
        RefreshTokenEntity stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token revoked/expired");
        }

        String username = jwtUtil.extractUsername(refreshToken);

        // 3) Rotate refresh token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccess = jwtUtil.generateToken(username);
        String newRefresh = jwtUtil.generateRefreshToken(username);
        saveRefreshToken(username, newRefresh);

        CookieUtil.addAccessCookie(response, newAccess);
        CookieUtil.addRefreshCookie(response, newRefresh);
    }

    private void saveRefreshToken(String username, String token) {
        Claims c = jwtUtil.extractClaims(token);
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.setUsername(username);
        e.setToken(token);
        e.setExpiresAt(c.getExpiration().toInstant());
        e.setRevoked(false);
        refreshTokenRepository.save(e);
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

}
