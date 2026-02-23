package com.example.mangxahoi.Service.Cache;

import com.example.mangxahoi.DTO.InfoUser.UserProfileCache;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.UserRepository;
import com.example.mangxahoi.Service.ImageService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class UserProfileCacheService {
    private final UserRepository userRepository;
    private final ImageService imageService;

    public UserProfileCacheService(UserRepository userRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Cacheable(value = "user_profile", key = "#userId")
    public UserProfileCache getProfile(Long userId){
        UserEntity u = userRepository.findById(userId).get();
        return fromEntity(u);
    }

    @CachePut(value = "user_profile", key = "#dto.id()")
    public UserProfileCache updateProfile(UserProfileCache dto){
        return dto;
    }

    public UserProfileCache fromEntity(UserEntity u) {
        return new UserProfileCache(
                u.getId(),
                imageService.buildImageUrl(u.getAvatar()),
                u.getFullName(),
                imageService.buildImageUrl(u.getCoverPhoto())
        );
    }
}
