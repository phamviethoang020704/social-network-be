package com.example.mangxahoi.Mapper;

import com.example.mangxahoi.DTO.Response.PostResponse;
import com.example.mangxahoi.Entity.LikeEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.CommentTargetType;
import com.example.mangxahoi.Enums.LikeTargetType;
import com.example.mangxahoi.Enums.ReactionType;
import com.example.mangxahoi.Enums.ShareType;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Service.ImageService;
import com.example.mangxahoi.Service.LikeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Component
public class PostMapper {
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final LikeService likeService;

    public PostResponse toResponse(PostEntity postEntity){
        UserEntity userEntity = userRepository.findById(postEntity.getUserEntity().getId()).orElseThrow(()->new RuntimeException("user not found"));
        Optional<LikeEntity> likeEntity = likeRepository.findByUserEntityAndLikeTargetTypeAndTargetId(userEntity,LikeTargetType.POST, postEntity.getId());
        return new PostResponse(
                postEntity.getId(),
                postEntity.getContent(),
                postEntity.getTypePost(),
                imageMapper.toResponseList(imageRepository.findByPostEntity(postEntity)),
                postEntity.getUpdatedAt(),
                tagMapper.toResponse(tagRepository.findByPostEntity(postEntity)),
                imageService.buildImageUrl(userEntity.getAvatar()),
                userEntity.getFullName(),
                userEntity.getId(),
                likeRepository.existsByUserEntityAndTargetIdAndLikeTargetType(userEntity,postEntity.getId(),LikeTargetType.POST),
                likeEntity.map(LikeEntity::getReactionType).orElse(null),
                likeRepository.countByLikeTargetTypeAndTargetId(LikeTargetType.POST,postEntity.getId()),
                commentRepository.countByCommentTargetTypeAndTargetId(CommentTargetType.POST,postEntity.getId()),
                shareRepository.countByShareTypeAndTargetId(ShareType.POST,postEntity.getId()),
                likeService.getFullReaction(postEntity.getId(),LikeTargetType.POST)
        );
    }
}
