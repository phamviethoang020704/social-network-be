package com.example.mangxahoi.Service.Search;

import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.DTO.Response.PostResponse;
import com.example.mangxahoi.DTO.Response.PosterInfoDTO;
import com.example.mangxahoi.DTO.Response.Search.ResultSearchGroup;
import com.example.mangxahoi.DTO.Response.Search.ResultSearchUser;
import com.example.mangxahoi.DTO.Response.Search.SearchListItem;
import com.example.mangxahoi.DTO.Response.Search.SearchListResponse;
import com.example.mangxahoi.DTO.Response.ShareResponse;
import com.example.mangxahoi.Entity.GroupEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Entity.ShareEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.*;
import com.example.mangxahoi.Mapper.PostMapper;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Repository.Projection.Reaction;
import com.example.mangxahoi.Service.ImageService;
import com.example.mangxahoi.Service.LikeService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchRenderService {

    private final SearchQueryService searchQueryService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PostRepository postRepository;
    private final ShareRepository shareRepository;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final LikeService likeService;
    private final ImageService imageService;
    private final FriendRepository friendRepository;
    private final GroupMemberRepository groupMemberRepository;

    private final PostMapper postMapper;

    public SearchRenderService(SearchQueryService searchQueryService,
                               UserRepository userRepository,
                               GroupRepository groupRepository,
                               PostRepository postRepository,
                               ShareRepository shareRepository,
                               ImageRepository imageRepository,
                               CommentRepository commentRepository,
                               LikeRepository likeRepository,
                               LikeService likeService,
                               ImageService imageService,
                               FriendRepository friendRepository,
                               GroupMemberRepository groupMemberRepository,
                               PostMapper postMapper) {
        this.searchQueryService = searchQueryService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.postRepository = postRepository;
        this.shareRepository = shareRepository;
        this.imageRepository = imageRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.likeService = likeService;
        this.imageService = imageService;
        this.friendRepository = friendRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.postMapper = postMapper;
    }

    @Transactional
    public SearchListResponse search(Long currentUserId, String q, String type, int page, int size) {

        String query = (q == null) ? "" : q.trim();
        if (query.isEmpty()) {
            return new SearchListResponse(query, type, Map.of(), page, size, List.of());
        }

        int offset = page * size;

        Map<String, Long> counts = searchQueryService.countByType(query);
        List<SearchQueryService.SearchHit> hits = searchQueryService.search(query, type, size, offset);

        // ===== group ids by type =====
        List<Long> userIds  = hits.stream().filter(h -> "USER".equals(h.type())).map(SearchQueryService.SearchHit::id).toList();
        List<Long> groupIds = hits.stream().filter(h -> "GROUP".equals(h.type())).map(SearchQueryService.SearchHit::id).toList();
        List<Long> postIds  = hits.stream().filter(h -> "POST".equals(h.type())).map(SearchQueryService.SearchHit::id).toList();
        List<Long> shareIds = hits.stream().filter(h -> "SHARE".equals(h.type())).map(SearchQueryService.SearchHit::id).toList();

        UserEntity currentUser = userRepository.findById(currentUserId).orElse(null);

        // ===== USER DTO =====
        Map<Long, ResultSearchUser> userDtoMap = buildUserResults(currentUserId, userIds);

        // ===== GROUP DTO =====
        Map<Long, ResultSearchGroup> groupDtoMap = buildGroupResults(currentUserId, groupIds);

        // ===== POST DTO =====
        Map<Long, PostResponse> postDtoMap = buildPostResponses(currentUser, postIds);

        // ===== SHARE DTO =====
        Map<Long, ShareResponse> shareDtoMap = buildShareResponses(currentUser, shareIds);

        // ===== Build items in hit order =====
        List<SearchListItem> items = new ArrayList<>();

        for (var h : hits) {
            Object data = switch (h.type()) {
                case "USER" -> userDtoMap.get(h.id());
                case "GROUP" -> groupDtoMap.get(h.id());
                case "POST" -> postDtoMap.get(h.id());
                case "SHARE" -> shareDtoMap.get(h.id());
                default -> null;
            };

            if (data != null) {
                items.add(new SearchListItem(h.type(), h.id(), h.score(), data));
            }
        }

        return new SearchListResponse(query, type, counts, page, size, items);
    }

    // =========================
    // USER results
    // =========================
    private Map<Long, ResultSearchUser> buildUserResults(Long currentUserId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();

        // accepted friend ids
        Set<Long> acceptedFriendIds = new HashSet<>(friendRepository.findFriendUserIds(currentUserId));

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        UserEntity::getId,
                        u -> new ResultSearchUser(
                                u.getId(),
                                imageService.buildImageUrl(u.getAvatar()),
                                u.getFullName(),
                                acceptedFriendIds.contains(u.getId()) ? 1L : 0L
                        )
                ));
    }

    // =========================
    // GROUP results
    // =========================
    private Map<Long, ResultSearchGroup> buildGroupResults(Long currentUserId, List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) return Map.of();

        Map<Long, GroupJoiningStatus> statusMap = new HashMap<>();
        for (Object[] r : groupMemberRepository.findStatusByUserAndGroupIds(currentUserId, groupIds)) {
            Long gid = (Long) r[0];
            GroupJoiningStatus st = (GroupJoiningStatus) r[1];
            statusMap.put(gid, st);
        }

        return groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(
                        GroupEntity::getId,
                        g -> new ResultSearchGroup(
                                g.getId(),
                                imageService.buildImageUrl(g.getCoverPhoto()),
                                g.getGroupName(),
                                statusMap.getOrDefault(g.getId(), null)
                        )
                ));
    }

    // =========================
    // POST responses (map + enrich)
    // =========================
    private Map<Long, PostResponse> buildPostResponses(UserEntity currentUser, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return Map.of();

        List<PostEntity> posts = postRepository.findAllById(postIds);

        // base mapping
        Map<Long, PostResponse> postMap = posts.stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toMap(PostResponse::getId, p -> p));

        if (currentUser == null) return postMap;

        // reaction type map (user -> post)
        Map<Long, ReactionType> reactionTypeMap =
                likeRepository.findUserReactions(currentUser, LikeTargetType.POST, postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Reaction::getTargetId,
                                Reaction::getReactionType
                        ));

        // like count map
        Map<Long, Long> likeMap =
                likeRepository.countLikes(LikeTargetType.POST, postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (Long) r[0],
                                r -> (Long) r[1]
                        ));

        // comment count map
        Map<Long, Long> commentMap =
                commentRepository.countCommentByTargetIds(postIds, CommentTargetType.POST)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (Long) r[0],
                                r -> (Long) r[1]
                        ));

        // share count map (shareType = POST)
        Map<Long, Long> shareCountMap = new HashMap<>();
        for (Object[] r : shareRepository.countSharesByTargetIds(ShareType.POST, postIds)) {
            shareCountMap.put((Long) r[0], (Long) r[1]);
        }

        // enrich each PostResponse
        for (Long pid : postIds) {
            PostResponse pr = postMap.get(pid);
            if (pr == null) continue;

            ReactionType rt = reactionTypeMap.get(pid);
            pr.setLiked(rt != null);
            pr.setReactionType(rt);

            pr.setLikeCount(likeMap.getOrDefault(pid, 0L));
            pr.setCommentCount(commentMap.getOrDefault(pid, 0L));
            pr.setShareCount(shareCountMap.getOrDefault(pid, 0L));

            // reactions full
            pr.setReactions(likeService.getFullReaction(pid, LikeTargetType.POST));
        }

        return postMap;
    }

    // =========================
    // SHARE responses
    // =========================
    private Map<Long, ShareResponse> buildShareResponses(UserEntity currentUser, List<Long> shareIds) {
        if (shareIds == null || shareIds.isEmpty()) return Map.of();
        if (currentUser == null) return Map.of();

        List<ShareEntity> shares = shareRepository.findAllById(shareIds);

        // Collect targetIds by type
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

        // reaction type map
        Map<Long, ReactionType> reactionTypeMap =
                likeRepository.findUserReactions(currentUser, LikeTargetType.SHARE, shareIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Reaction::getTargetId,
                                Reaction::getReactionType
                        ));

        // like count map
        Map<Long, Long> likeMap =
                likeRepository.countLikes(LikeTargetType.SHARE, shareIds)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (Long) r[0],
                                r -> (Long) r[1]
                        ));

        // comment count map
        Map<Long, Long> commentMap =
                commentRepository.countCommentByTargetIds(shareIds, CommentTargetType.SHARE)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (Long) r[0],
                                r -> (Long) r[1]
                        ));

        // poster info
        Map<Long, PosterInfoDTO> posterByPostId = sharedPostIds.isEmpty()
                ? Map.of()
                : postRepository.findPosterInfoByPostIds(sharedPostIds)
                .stream().collect(Collectors.toMap(
                        PosterInfoDTO::targetId,
                        p -> p
                ));

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
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        // build share responses
        return shares.stream().collect(Collectors.toMap(
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

                            s.getShareType() == ShareType.POST
                                    ? imageByPostMap.getOrDefault(s.getTargetId(), List.of())
                                    : List.of(),

                            s.getShareType() == ShareType.POST
                                    ? contentPostMap.get(s.getTargetId())
                                    : null,

                            s.getShareType() == ShareType.IMAGE
                                    ? imageService.buildImageUrl(imageByImageMap.get(s.getTargetId()))
                                    : null,

                            realPostId,

                            poster != null ? poster.id() : null,
                            poster != null ? imageService.buildImageUrl(poster.avatar()) : null,
                            poster != null ? poster.fullName() : null,
                            poster != null ? poster.updatedAt() : null,

                            reactionType != null,
                            reactionType,

                            likeMap.getOrDefault(s.getId(), 0L),
                            commentMap.getOrDefault(s.getId(), 0L),
                            likeService.getFullReaction(s.getId(), LikeTargetType.SHARE),

                            s.getUserEntity().getId(),
                            s.getUserEntity().getFullName(),
                            imageService.buildImageUrl(s.getUserEntity().getAvatar())
                    );
                }
        ));
    }
}
