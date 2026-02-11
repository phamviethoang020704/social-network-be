package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.DTO.Response.ImageViewResponse;
import com.example.mangxahoi.Entity.ImageEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.CommentTargetType;
import com.example.mangxahoi.Enums.LikeTargetType;
import com.example.mangxahoi.Enums.ReactionType;
import com.example.mangxahoi.Enums.ShareType;
import com.example.mangxahoi.Repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final LikeService likeService;
    @Value("${app.upload.dir}")
    private String uploadDir;

    public ImageService(ImageRepository imageRepository, LikeRepository likeRepository, UserRepository userRepository, CommentRepository commentRepository, ShareRepository shareRepository, LikeService likeService) {
        this.imageRepository = imageRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.shareRepository = shareRepository;
        this.likeService = likeService;
    }

    public String uploadImage(MultipartFile file, String subFolder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String folder = uploadDir + "/" + subFolder + "/";
        Files.createDirectories(Paths.get(folder));

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(folder, fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/images-mangxahoi/" + subFolder + "/" + fileName;
    }

    public ImageViewResponse getImageView(Long imageId, Long postId,String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));

        ImageEntity current = imageRepository.findById(imageId).orElseThrow(() -> new RuntimeException("Image not found"));
        List<ImageEntity> images = imageRepository.findByPostEntityIdOrderByIdAsc(postId);
        if(images.isEmpty()){
            throw new RuntimeException("Post has no images");
        }
        int index = -1;
        for(int i = 0; i < images.size(); i++){
            if(images.get(i).getId().equals(imageId)){
                index = i;
                break;
            }
        }
        if(index == -1){
            throw new RuntimeException("Image not belong to post");
        }
        ImageEntity prev = index > 0 ? images.get(index - 1) : null;
        ImageEntity next = index < images.size() - 1 ? images.get(index + 1) : null;

        Optional<ReactionType> reactionOpt =
                likeRepository.findReactionByUserAndTargetAndTargetId(user.getId(), current.getId(),LikeTargetType.IMAGE);
        return new ImageViewResponse(
                new ImageResponse(
                        current.getId(),
                        buildImageUrl(current.getImageUrl())
                ),
                prev == null ? null : new ImageResponse(
                        prev.getId(),
                        buildImageUrl(prev.getImageUrl())
                ),
                next == null ? null : new ImageResponse(
                        next.getId(),
                        buildImageUrl(next.getImageUrl())
                ),

                current.getUpdatedAt(),

                buildImageUrl(current.getPostEntity().getUserEntity().getAvatar()),
                current.getPostEntity().getUserEntity().getFullName(),
                current.getPostEntity().getUserEntity().getId(),
                reactionOpt.isPresent(),
                reactionOpt.orElse(null),

                likeRepository.countByLikeTargetTypeAndTargetId(LikeTargetType.IMAGE,current.getId()),
                commentRepository.countByCommentTargetTypeAndTargetId(CommentTargetType.IMAGE, current.getId()),
                shareRepository.countByShareTypeAndTargetId(ShareType.IMAGE, current.getId()),
                likeService.getFullReaction(current.getId(), LikeTargetType.IMAGE)
        );
    }
    public String buildImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }

        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(relativePath)
                .toUriString();
    }
}
