package com.example.mangxahoi.Service.Impl;

import com.example.mangxahoi.DTO.InfoUser.AvatarUser;
import com.example.mangxahoi.DTO.InfoUser.ChangeImage;
import com.example.mangxahoi.DTO.Response.FeedResponse;
import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.DTO.Response.PostResponse;
import com.example.mangxahoi.DTO.UserProfileResponse;
import com.example.mangxahoi.Entity.FeedItemEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Entity.ImageEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.FeedType;
import com.example.mangxahoi.Enums.PostType;
import com.example.mangxahoi.Enums.SearchType;
import com.example.mangxahoi.Mapper.ImageMapper;
import com.example.mangxahoi.Mapper.PostMapper;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Service.AuthService;
import com.example.mangxahoi.Service.ImageService;
import com.example.mangxahoi.Service.Search.SearchRenderService;
import com.example.mangxahoi.Service.Search.SearchService;
import com.example.mangxahoi.Service.Search.UpsertService;
import com.example.mangxahoi.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final PostRepository postRepository;
    private final AuthService authService;
    private final FriendRepository friendRepository;
    private final ImageMapper imageMapper;
    private final ImageRepository imageRepository;
    private final PostMapper postMapper;
    private final FeedItemRepository feedItemRepository;
    private final SearchRenderService searchRenderService;
    private final SearchService searchService;
    private final UpsertService upsertService;
    @Value("${app.default.avatar.male}")
    private String maleDefaultAvatar;

    @Value("${app.default.avatar.female}")
    private String femaleDefaultAvatar;

    @Value("${app.default.cover-photo}")
    private String coverPhotoDefault;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ImageService imageService;
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, PostRepository postRepository, ImageService imageService , AuthService authService, FriendRepository friendRepository, ImageMapper imageMapper, ImageRepository imageRepository, PostMapper postMapper, FeedItemRepository feedItemRepository, SearchRenderService searchRenderService, SearchService searchService, UpsertService upsertService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.postRepository = postRepository;
        this.imageService = imageService;
        this.authService = authService;
        this.friendRepository = friendRepository;
        this.imageMapper = imageMapper;
        this.imageRepository = imageRepository;
        this.postMapper = postMapper;
        this.feedItemRepository = feedItemRepository;
        this.searchRenderService = searchRenderService;
        this.searchService = searchService;
        this.upsertService = upsertService;
    }


    @Override
    @Transactional
    public FeedResponse changImageUser(
            ChangeImage changeImage,
            MultipartFile image,
            String username
    ) throws IOException {

        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Image is required");
        }

        if (changeImage.postType() != PostType.AVATAR && changeImage.postType() != PostType.COVER_PHOTO) {
            throw new RuntimeException("PostType is required");
        }

        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("user not found")
        );



        //tạo post
        userRepository.save(userEntity);
        PostEntity postEntity = new PostEntity();
        postEntity.setUserEntity(userEntity);
        postEntity.setContent(changeImage.content());
        postEntity.setTypePost(changeImage.postType());
        PostEntity savePostE = postRepository.save(postEntity);
        upsertService.upsert(SearchType.POST,savePostE.getId(),savePostE.getContent());
        String imageUrl;

        switch (changeImage.postType()) {
            case AVATAR -> {
                imageUrl = uploadUserImage(image, "avt-user");
                userEntity.setAvatar(imageUrl);
                userEntity.setAvatarPostId(savePostE.getId());
            }
            case COVER_PHOTO -> {
                imageUrl = uploadUserImage(image, "cover-photo");
                userEntity.setCoverPhoto(imageUrl);
                userEntity.setCoverPostId(savePostE.getId());
            }
            default -> throw new RuntimeException("Invalid post type");
        }
        userRepository.save(userEntity);

        // tạo image
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setImageUrl(imageUrl);
        imageEntity.setPostEntity(savePostE);
        imageRepository.save(imageEntity);

        //build PostResponse
        PostResponse postResponse = postMapper.toResponse(savePostE);

        //build FeedResponse
        FeedItemEntity feedItemEntity = new FeedItemEntity();
        feedItemEntity.setFeedType(FeedType.POST);
        feedItemEntity.setRefId(savePostE.getId());
        feedItemEntity.setUpdatedAt(LocalDateTime.now());
        feedItemEntity.setUserEntity(userEntity);
        FeedItemEntity saveFeed = feedItemRepository.save(feedItemEntity);

        //build feedResponse
        FeedResponse feedResponse = new FeedResponse(
                saveFeed.getId(),
                FeedType.POST,
                saveFeed.getUpdatedAt(),
                postResponse,
                null
        );
        return feedResponse;
    }

    //lấy ra id user hiển thị kết bạn, avt,name,số bb
    @Override
    public UserProfileResponse getUserProfile(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String avatarUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(user.getAvatar())
                .toUriString();
        String coverPhotoUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(user.getCoverPhoto())
                .toUriString();
        String fullName = user.getFullName();
        String username = user.getUsername();
        Long countFriend = friendRepository.countFriendsByUserId(user.getId());
        return new UserProfileResponse(id, username, countFriend,fullName,avatarUrl,coverPhotoUrl);
    }

    //hàm lấy avt user đang đăng nhập trả về header
    @Override
    public AvatarUser getAvatar(HttpServletRequest request){
        UserEntity userEntity = authService.getCurrentUser();
        return new AvatarUser(userEntity.getId(),imageService.buildImageUrl(userEntity.getAvatar()), userEntity.getFullName());
    }

    private String uploadUserImage(MultipartFile image, String subFolder) throws IOException {
        String folder = Paths.get(uploadDir, subFolder).toString();
        Files.createDirectories(Paths.get(folder));

        String original = Paths.get(image.getOriginalFilename())
                .getFileName()
                .toString();

        String fileName = UUID.randomUUID() + "_" + original;
        Path filePath = Paths.get(folder, fileName);

        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/" + subFolder + "/" + fileName;
    }

}
