package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Request.ShareRequest;
import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.DTO.Response.ShareResponse;
import com.example.mangxahoi.Entity.*;
import com.example.mangxahoi.Enums.*;
import com.example.mangxahoi.Mapper.ImageMapper;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Service.Search.UpsertService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ShareService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final ShareRepository shareRepository;
    private final ImageMapper imageMapper;
    private final FeedItemRepository feedItemRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final LikeService likeService;
    private final ImageService imageService;
    private final UpsertService searchService;
    private final NotificationService notificationService;

    @Transactional
    public void shareTarget(ShareRequest shareRequest, String username){
        UserEntity userEntity = userRepository.findByUsername(username).get();
        if (shareRepository.existsByUserEntityAndTargetIdAndShareType(userEntity, shareRequest.targetId(),  shareRequest.shareType())) {
            throw new RuntimeException("bài này bạn đã share rồi");
        }
        if (shareRequest.shareType() == ShareType.POST){
            PostEntity postEntity = postRepository.findById(shareRequest.targetId()).get();
            if(postEntity.getUserEntity().getId().equals(userEntity.getId())){
                throw new RuntimeException("không thể share Post của bạn");
            }
        } else if (shareRequest.shareType() == ShareType.IMAGE) {
            ImageEntity imageEntity = imageRepository.findById(shareRequest.targetId()).get();
            if(imageEntity.getPostEntity().getUserEntity().getId().equals(userEntity.getId())){
                throw new RuntimeException("không thể tự share ảnh của bạn");
            }
        }
        ShareEntity shareEntity = new ShareEntity();
        shareEntity.setTargetId(shareRequest.targetId());
        shareEntity.setShareType(shareRequest.shareType());
        shareEntity.setCaption(shareRequest.caption());
        shareEntity.setUserEntity(userEntity);
        ShareEntity saveShare = shareRepository.save(shareEntity);

        if (shareRequest.shareType() == ShareType.POST) {
            PostEntity postEntity = postRepository.findById(shareRequest.targetId())
                    .orElseThrow(() -> new RuntimeException("post not found"));

            notificationService.createPostShared(
                    userEntity,
                    postEntity.getUserEntity(),
                    postEntity.getId(),
                    saveShare.getId()
            );
        }
        FeedItemEntity  feedItemEntity = new FeedItemEntity();
        feedItemEntity.setUpdatedAt(LocalDateTime.now());
        feedItemEntity.setFeedType(FeedType.SHARE);
        feedItemEntity.setUserEntity(userEntity);
        feedItemEntity.setRefId(saveShare.getId());
        feedItemRepository.save(feedItemEntity);

        searchService.upsert(SearchType.SHARE,saveShare.getId(),saveShare.getCaption());
    }
    @Transactional
    public ShareResponse editShareTarget(Long shareId, ShareRequest request, String username){
        UserEntity userEntity = userRepository.findByUsername(username).get();
        ShareEntity shareEntity = shareRepository.findById(shareId).orElseThrow(
                () -> new RuntimeException("share not found")
        );
        if (!shareEntity.getUserEntity().getId().equals(userEntity.getId())){
            throw new RuntimeException(" người dùng không có quyền sửa");
        }
        shareEntity.setCaption(request.caption());
        ShareEntity savedShare = shareRepository.save(shareEntity);

        FeedItemEntity feedItemEntity = feedItemRepository.findByRefIdAndFeedType(shareId,FeedType.SHARE).get();
        feedItemEntity.setUpdatedAt(LocalDateTime.now());
        feedItemRepository.save(feedItemEntity);
        //
        List<ImageResponse> imagesByPost = new ArrayList<>();
        String imageByImage = null;
        String contentPost = null;
        PostEntity postEntity = null;
        if (shareEntity.getShareType() == ShareType.POST){
            postEntity = postRepository.findById(request.targetId()).get();
            imagesByPost = imageMapper.toResponseList(postEntity.getImageEntity());
            contentPost = postEntity.getContent();

        }
        if (shareEntity.getShareType() == ShareType.IMAGE) {
            ImageEntity imageEntity = imageRepository.findById(request.targetId()).get();
            imageByImage = imageService.buildImageUrl(imageEntity.getImageUrl());
            postEntity = imageEntity.getPostEntity();
        }

        ReactionType reactionType = likeRepository
                .findByUserEntityAndLikeTargetTypeAndTargetId(
                        userEntity,
                        LikeTargetType.SHARE,
                        savedShare.getId()
                )
                .map(LikeEntity::getReactionType)
                .orElse(null);

        searchService.upsert(SearchType.SHARE,savedShare.getId(),savedShare.getCaption());
        return new ShareResponse(
                savedShare.getId(),
                savedShare.getCaption(),
                savedShare.getShareType(),
                savedShare.getTargetId(),
                savedShare.getUpdatedAt(),
                imagesByPost,
                contentPost,
                imageByImage,

                postEntity.getId(),
                postEntity.getUserEntity().getId(),
                postEntity.getUserEntity().getAvatar(),
                postEntity.getUserEntity().getFullName(),
                postEntity.getUpdatedAt(),

                likeRepository.existsByUserEntityAndTargetIdAndLikeTargetType(userEntity,savedShare.getId(),LikeTargetType.SHARE),
                reactionType,
                likeRepository.countByLikeTargetTypeAndTargetId(LikeTargetType.SHARE, savedShare.getId()),
                commentRepository.countByCommentTargetTypeAndTargetId(CommentTargetType.SHARE, savedShare.getId()),
                likeService.getFullReaction(savedShare.getId(),LikeTargetType.SHARE),
                shareEntity.getUserEntity().getId(),
                shareEntity.getUserEntity().getFullName(),
                imageService.buildImageUrl(shareEntity.getUserEntity().getAvatar())
        );
    }
    @Transactional
    public void deleteShareTarget(Long shareId, String username){
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("user not found")
        );
        ShareEntity shareEntity = shareRepository.findById(shareId).orElseThrow(
                () -> new RuntimeException("share not found")
        );
        if (!shareEntity.getUserEntity().getId().equals(userEntity.getId())){
            throw new RuntimeException("người dùng không có quyền xóa");
        }
        FeedItemEntity feedItemEntity = feedItemRepository.findByRefIdAndFeedType(shareId,FeedType.SHARE).get();
        feedItemRepository.delete(feedItemEntity);

        //xóa những like vào bài share
        likeRepository.deleteAllByTargetIdAndLikeTargetType(shareEntity.getId(),LikeTargetType.SHARE);

        //xóa những like vào những cmt của bài share
        List<Long> commentIds = commentRepository.getCommentIdsByTargetAndType(List.of(shareEntity.getId()), CommentTargetType.SHARE);
        likeRepository.deleteByTargetIdsAndLikeTargetType(commentIds, LikeTargetType.COMMENT);

        //xóa comment vào bài share
        commentRepository.deleteByTargetIdsAndType(List.of(shareEntity.getId()),CommentTargetType.SHARE);

        shareRepository.delete(shareEntity);
    }

    public ShareResponse getShareByShareId(Long shareId, String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ShareEntity share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("share not found"));

        List<ImageResponse> imagesByPost = new ArrayList<>();
        String imageByImage = null;
        String contentPost = null;

        PostEntity postEntity = null;

        if (share.getShareType() == ShareType.POST) {

            postEntity = postRepository.findById(share.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            imagesByPost = imageMapper.toResponseList(
                    postEntity.getImageEntity()
            );

            contentPost = postEntity.getContent();
        }

        if (share.getShareType() == ShareType.IMAGE) {

            ImageEntity imageEntity = imageRepository
                    .findById(share.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Image not found"));

            imageByImage = imageService.buildImageUrl(
                    imageEntity.getImageUrl()
            );

            postEntity = imageEntity.getPostEntity();
        }

        ReactionType reactionType = likeRepository
                .findByUserEntityAndLikeTargetTypeAndTargetId(
                        userEntity,
                        LikeTargetType.SHARE,
                        share.getId()
                )
                .map(LikeEntity::getReactionType)
                .orElse(null);

        return new ShareResponse(

                share.getId(),
                share.getCaption(),
                share.getShareType(),
                share.getTargetId(),
                share.getUpdatedAt(),

                imagesByPost,
                contentPost,
                imageByImage,

                postEntity.getId(),
                postEntity.getUserEntity().getId(),
                imageService.buildImageUrl(postEntity.getUserEntity().getAvatar()),
                postEntity.getUserEntity().getFullName(),
                postEntity.getUpdatedAt(),

                likeRepository.existsByUserEntityAndTargetIdAndLikeTargetType(
                        userEntity,
                        share.getId(),
                        LikeTargetType.SHARE
                ),

                reactionType,

                likeRepository.countByLikeTargetTypeAndTargetId(
                        LikeTargetType.SHARE,
                        share.getId()
                ),

                commentRepository.countByCommentTargetTypeAndTargetId(
                        CommentTargetType.SHARE,
                        share.getId()
                ),

                likeService.getFullReaction(
                        share.getId(),
                        LikeTargetType.SHARE
                ),

                share.getUserEntity().getId(),
                share.getUserEntity().getFullName(),

                imageService.buildImageUrl(
                        share.getUserEntity().getAvatar()
                )
        );
    }

}
























