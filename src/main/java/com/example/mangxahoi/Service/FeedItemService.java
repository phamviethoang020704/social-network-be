package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Response.*;
import com.example.mangxahoi.Entity.FeedItemEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Entity.ShareEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.*;
import com.example.mangxahoi.Mapper.PostMapper;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Repository.Projection.Reaction;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedItemService {
    private final FeedItemRepository feedItemRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final ShareRepository shareRepository;
    private final LikeService likeService;
    private final ImageService imageService;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public FeedItemService(FeedItemRepository feedItemRepository, PostRepository postRepository, PostMapper postMapper, ShareRepository shareRepository, LikeService likeService, ImageService imageService, CommentRepository commentRepository, LikeRepository likeRepository, ImageRepository imageRepository, UserRepository userRepository) {
        this.feedItemRepository = feedItemRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.shareRepository = shareRepository;
        this.likeService = likeService;
        this.imageService = imageService;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    public FeedSliceResponse allFeed(int size, LocalDateTime cursorTime, Long cursorId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
        Pageable pageable = PageRequest.of(0, size + 1);
        List<FeedItemEntity> rows;

        if (cursorTime == null || cursorId == null) {
            rows = feedItemRepository.findAllFeedFirstPage(pageable);
        } else {
            rows = feedItemRepository.findAllFeedNextPage(cursorTime, cursorId, pageable);
        }

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        List<Long> postIds = new ArrayList<>();
        List<Long> shareIds = new ArrayList<>();
        for(FeedItemEntity f : rows){
            if (f.getFeedType() == FeedType.POST) postIds.add(f.getRefId());
            else shareIds.add(f.getRefId());
        }

        Map<Long, PostResponse> postMap = Map.of();
        if(!postIds.isEmpty()){
            List<PostEntity> postEntities = postRepository.findAllById(postIds);
            postMap = postEntities.stream()
                    .map(postMapper::toResponse)
                    .collect(Collectors.toMap(PostResponse::getId, p -> p));
        }

        // ===== Load Shares in batch =====
        Map<Long, ShareResponse> shareMap = Map.of();
        if (!shareIds.isEmpty()) {
            List<ShareEntity> shares = shareRepository.findAllById(shareIds);

            // Collect targetIds by type for batch enrichment
            List<Long> sharedPostIds = new ArrayList<>();
            List<Long> sharedImageIds = new ArrayList<>();
            for (ShareEntity s : shares) {
                if (s.getShareType() == ShareType.POST) sharedPostIds.add(s.getTargetId());
                else if (s.getShareType() == ShareType.IMAGE) sharedImageIds.add(s.getTargetId());
            }

            // content of shared posts
            Map<Long, String> contentPostMap = sharedPostIds.isEmpty()
                    ? Map.of()
                    : postRepository.getContentByPostIds(sharedPostIds).stream()
                    .collect(Collectors.toMap(
                            r -> (Long) r[0],
                            r -> (String) r[1]
                    ));

            // images of shared posts
            Map<Long, List<ImageResponse>> imageByPostMap = new HashMap<>();
            if (!sharedPostIds.isEmpty()) {
                for (Object[] r : imageRepository.getImagesByPostIds(sharedPostIds)) {
                    Long postId = (Long) r[0];
                    ImageResponse img = new ImageResponse();
                    img.setId((Long) r[1]);
                    img.setImageUrl(imageService.buildImageUrl((String) r[2]));
                    imageByPostMap.computeIfAbsent(postId, k -> new ArrayList<>()).add(img);
                }
            }

            // url of shared images
            Map<Long, String> imageByImageMap = sharedImageIds.isEmpty()
                    ? Map.of()
                    : imageRepository.getImageUrlByIds(sharedImageIds).stream()
                    .collect(Collectors.toMap(
                            r -> (Long) r[0],
                            r -> (String) r[1]
                    ));
            //kiểm tra isLiked và reactionType
            Map<Long, ReactionType> reactionTypeMap =
                    likeRepository.findUserReactions(userEntity, LikeTargetType.SHARE, shareIds)
                            .stream().collect(Collectors.toMap(
                                    Reaction::getTargetId,
                                    Reaction::getReactionType
                            ));
            //Lấy ra countLike
            Map<Long,Long> likeMap =
                    likeRepository.countLikes(LikeTargetType.SHARE,shareIds)
                            .stream().collect(Collectors.toMap(
                                    r -> (Long) r[0],
                                    r -> (Long) r[1]
                            ));

            //Lấy ra countComment
            Map<Long,Long> commentMap =
                    commentRepository.countCommentByTargetIds(shareIds, CommentTargetType.SHARE)
                            .stream().collect(Collectors.toMap(
                                    r -> (Long) r[0],
                                    r  -> (Long) r[1]
                            ));
            //lấy ra thông tin người đăng bài của bài chia sẻ
                //theo postId
            Map<Long, PosterInfoDTO> posterByPostId = sharedPostIds.isEmpty()
                    ? Map.of()
                    : postRepository.findPosterInfoByPostIds(sharedPostIds)
                    .stream().collect(Collectors.toMap(
                            PosterInfoDTO::targetId,
                            p -> p
                    ));
                //theo imageId
            Map<Long, PosterInfoDTO> posterByImageId = sharedImageIds.isEmpty()
                    ? Map.of()
                    : imageRepository.findPosterInfoByImageIds(sharedImageIds)
                    .stream().collect(Collectors.toMap(
                            PosterInfoDTO::targetId,
                            p -> p
                    ));
            // imageId -> postId
            Map<Long, Long> postIdByImageId = sharedImageIds.isEmpty()
                    ? Map.of()
                    : imageRepository.findPostIdByImageIds(sharedImageIds)
                    .stream()
                    .collect(Collectors.toMap(
                            r -> (Long) r[0], // imageId
                            r -> (Long) r[1]  // postId
                    ));
            // build share responses
            shareMap = shares.stream().collect(Collectors.toMap(
                    ShareEntity::getId,
                    s -> {
                        ReactionType reactionType = reactionTypeMap.get(s.getId());
                        PosterInfoDTO poster =
                                s.getShareType() == ShareType.POST
                                        ? posterByPostId.get(s.getTargetId())
                                        : posterByImageId.get(s.getTargetId());

                        Long realPostId =
                                s.getShareType() == ShareType.POST
                                        ? s.getTargetId()
                                        : postIdByImageId.get(s.getTargetId());
                        return new ShareResponse(
                                s.getId(),
                                s.getCaption(),
                                s.getShareType(),
                                s.getTargetId(),
                                s.getUpdatedAt(),
                                s.getShareType() == ShareType.POST ? imageByPostMap.getOrDefault(s.getTargetId(), List.of()) : List.of(),
                                s.getShareType() == ShareType.POST ? contentPostMap.get(s.getTargetId()) : null,
                                s.getShareType() == ShareType.IMAGE ? imageService.buildImageUrl(imageByImageMap.get(s.getTargetId())) : null,

                                realPostId,
                                poster != null ? poster.id() : null,
                                poster != null ? imageService.buildImageUrl(poster.avatar()) : null,
                                poster != null ? poster.fullName() : null,
                                poster != null ? poster.updatedAt() : null,

                                reactionType != null,
                                reactionType,
                                likeMap.getOrDefault(s.getId(),0L),
                                commentMap.getOrDefault(s.getId(),0L),
                                likeService.getFullReaction(s.getId(),LikeTargetType.SHARE),
                                s.getUserEntity().getId(),
                                s.getUserEntity().getFullName(),
                                imageService.buildImageUrl(s.getUserEntity().getAvatar())
                        );
                    }
            ));
        }

        // ===== Build FeedResponse in original order =====
        List<FeedResponse> items = new ArrayList<>(rows.size());
        for (FeedItemEntity f : rows) {
            FeedResponse fr = new FeedResponse();
            fr.setId(f.getId());
            fr.setFeedType(f.getFeedType());
            fr.setUpdatedAt(f.getUpdatedAt());

            if (f.getFeedType() == FeedType.POST) {
                fr.setPost(postMap.get(f.getRefId())); // can be null if deleted
                fr.setShare(null);
            } else {
                fr.setPost(null);
                fr.setShare(shareMap.get(f.getRefId()));
            }
            items.add(fr);
        }

        // next cursor = last item
        LocalDateTime nextTime = null;
        Long nextId = null;
        if (!items.isEmpty()) {
            FeedResponse last = items.get(items.size() - 1);
            nextTime = last.getUpdatedAt();
            nextId = last.getId();
        }

        return new FeedSliceResponse(items, hasNext, nextTime, nextId);
    }
}
