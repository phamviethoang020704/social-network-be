package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.InfoUser.AvatarUser;
import com.example.mangxahoi.DTO.InfoUser.ChangeImage;
import com.example.mangxahoi.DTO.Response.FeedResponse;
import com.example.mangxahoi.DTO.Response.PostResponse;
import com.example.mangxahoi.DTO.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    FeedResponse changImageUser(ChangeImage changeImage, MultipartFile avatar, String username) throws IOException;
    UserProfileResponse getUserProfile(Long id);
    AvatarUser getAvatar(HttpServletRequest request);
}
