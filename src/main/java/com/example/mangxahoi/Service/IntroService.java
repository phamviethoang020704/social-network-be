package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.InfoUser.AddressDTO;
import com.example.mangxahoi.DTO.InfoUser.PersonalDetails;
import com.example.mangxahoi.DTO.Response.*;
import com.example.mangxahoi.Entity.*;
import com.example.mangxahoi.Enums.*;
import com.example.mangxahoi.Mapper.ImageMapper;
import com.example.mangxahoi.Mapper.PostMapper;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Repository.Projection.Reaction;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IntroService  {

    private final UserRepository userRepository;
    private final PostService postService;
    private final FriendService friendService;
    private final FriendRepository friendRepository;
    private final ImageRepository imageRepository;
    private final PostMapper postMapper;
    private final ImageMapper imageMapper;
    private final ImageService imageService;
    private final ShareRepository shareRepository;
    private final PostRepository postRepository;
    private final FeedItemRepository feedItemRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final LikeService likeService;
    private final ProvinceRepo provinceRepo;
    private final DistrictRepo districtRepo;
    private final WardRepo wardRepo;

    public IntroResponse buildIntro(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("user not found")
        );
        IntroResponse introResponse = new IntroResponse();
        //thông tin cơ bản
        introResponse.setIntroduce(userEntity.getIntroduce());
        introResponse.setWork(userEntity.getWork());
        introResponse.setProvinceName(userEntity.getProvinceName());
        introResponse.setUniversity(userEntity.getUniversity());

        //list post của user
//        List<PostEntity> postEntities = postService.getPostsByUserId(userId);
//        List<PostResponse> postResponses = postEntities.stream().map(postMapper::toResponse).toList();
//        introResponse.setPosts(postResponses);


        //lấy ra List friend của user max = 9
        List<FriendEntity> friendEntities =
                friendRepository.findTop9RecentFriends(
                        userEntity.getId(),
                        PageRequest.of(0, 9)
                );
        List<FriendResponse> friendResponses = friendEntities.stream().map(f -> {
            UserEntity friend =
                    f.getUserSend().getId().equals(userEntity.getId())
                            ? f.getUserAccept()
                            : f.getUserSend();
            FriendResponse res = new  FriendResponse();
            res.setId(friend.getId());
            res.setFullName(friend.getFullName());
            res.setAvatar(imageService.buildImageUrl(friend.getAvatar()));
            return res;
        }).toList();
        introResponse.setFriends(friendResponses);
        //Lấy ra List ảnh của user max = 9
        List<ImageEntity> imageEntities = imageRepository.findTop9ByPostEntity_UserEntityAndPostEntity_GroupEntityIsNullOrderByCreatedAtDesc(userEntity);
        List<ImageResponse> imageResponses = imageMapper.toResponseList(imageEntities);
        introResponse.setImages(imageResponses);
        return  introResponse;
    }

    //Lấy ra Posts,Shares của user theo Id
    public FeedSliceResponse feedUserById(Long userId, int size, LocalDateTime cursorTime, Long cursorId) {
        // validate user exists
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));

        Pageable pageable = PageRequest.of(0, size + 1);
        List<FeedItemEntity> rows;

        if (cursorTime == null || cursorId == null) {
            rows = feedItemRepository.findAllFeedFirstPage(pageable);
        } else {
            rows = feedItemRepository.findAllFeedNextPage(cursorTime, cursorId, pageable);
        }

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        // split ids
        List<Long> postIds = new ArrayList<>();
        List<Long> shareIds = new ArrayList<>();
        for (FeedItemEntity f : rows) {
            if (f.getFeedType() == FeedType.POST) postIds.add(f.getRefId());
            else shareIds.add(f.getRefId());
        }

        // ===== Load Posts in batch =====
        Map<Long, PostResponse> postMap = Map.of();
        if (!postIds.isEmpty()) {
            List<PostEntity> posts = postRepository.findAllById(postIds);

            // map -> PostResponse
            postMap = posts.stream()
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

            //lấy ra info của người đăng bài của bài chia sẻ
                //lấy theo postId
            Map<Long, PosterInfoDTO> posterByPostId = sharedPostIds.isEmpty()
                    ? Map.of()
                    : postRepository.findPosterInfoByPostIds(sharedPostIds)
                    .stream().collect(Collectors.toMap(
                            PosterInfoDTO::targetId,
                            p -> p
                    ));
                //lấy theo imageId
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

    // lấy all thông tin user
    public AboutResponse getAbout(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).get();

        return new AboutResponse(
                userEntity.getUniversity(),
                userEntity.getHighSchool(),
                userEntity.getWork(),
                userEntity.getWardCode(),
                userEntity.getWardName(),
                userEntity.getDistrictCode(),
                userEntity.getDistrictName(),
                userEntity.getProvinceCode(),
                userEntity.getProvinceName(),
                userEntity.getBirthday(),
                userEntity.getPhoneNumber(),
                userEntity.getGender(),
                userEntity.getSocialLink(),
                userEntity.getCreatedAt(),
                userEntity.getIntroduce(),
                userEntity.getBiography()
        );
    }

    // lấy ra all friends của user
    public List<FriendResponse> getAllFriends(Long userId, int page){
        Pageable pageable = PageRequest.of(page,10);
        Page<FriendResponse> pageResponse = friendRepository.getAllFriends(userId,pageable);
        List<FriendResponse> friends = pageResponse.getContent();
        return friends.stream().map(
                f -> {
                    f.setAvatar(
                            imageService.buildImageUrl(f.getAvatar())
                    );
                    return f;
                }
        ).toList();
    }
    //lấy ra tất cả bạn chung của user
    public List<FriendResponse> getMutualFriends(Long ownerId, String username) {

        UserEntity viewer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (viewer.getId().equals(ownerId)) {
            return List.of(); // immutable, rõ nghĩa
        }

        return friendRepository.getMutualFriends(ownerId, viewer.getId()).stream().map(
                f -> {
                    f.setAvatar(
                            imageService.buildImageUrl(f.getAvatar())
                    );
                    return f;
                }
        ).toList();
    }

    //lấy ra tất cả ảnh của user
    public List<ImageResponse> getAllImagesByUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return imageRepository.getAllImagesByUser(userId).stream().map(
                i -> {
                    i.setImageUrl(imageService.buildImageUrl(i.getImageUrl()));
                    return i;
                }
        ).toList();
    }

    //sửa intro,bio
    public Map<String, String> editIntro(Map<String, String> body, String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.setIntroduce(body.get("intro"));
        user.setBiography(body.get("bio"));
        userRepository.save(user);
        return Map.of(
                "introduce", body.get("intro"),
                "biography", body.get("bio")
        );
    }

    //sửa work
    public Map<String, String> editWork(
            Map<String, String> body,
            String username
    ) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String work = body.get("work");

        user.setWork(work);
        userRepository.save(user);

        return Map.of(
                "work", work
        );
    }

    //sủa education,hight-school
    public Map<String, String> editEducation(
            Map<String, String> body,
            String username
    ) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        String university  = body.get("university");
        String highSchool  = body.get("high_school");

        user.setUniversity(university);
        user.setHighSchool(highSchool);

        userRepository.save(user);

        return Map.of(
                "university", university,
                "high_school", highSchool
        );
    }

    //sửa birthday,gender,phone
    public PersonalDetails editPersonalDetails(
            PersonalDetails data,
            String username
    ) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

       user.setPhoneNumber(data.phoneNumber());
       user.setBirthday(data.birthday());
       user.setGender(data.genderUser());
       UserEntity savedUser = userRepository.save(user);
       return new PersonalDetails(
               savedUser.getBirthday(),
               savedUser.getGender(),
               savedUser.getPhoneNumber()
       );
    }

    //sư address
    public AddressDTO editAddress(
            AddressDTO request,
            String username
    ) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Province p = provinceRepo.findById(request.provinceCode())
                .orElseThrow(() -> new RuntimeException("Province not found"));
        District d = districtRepo.findById(request.districtCode())
                .orElseThrow(() -> new RuntimeException("District not found"));
        Ward w = wardRepo.findById(request.wardCode())
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        // check district thuộc province, ward thuộc district
        if (!d.getProvinceCode().equals(p.getCode()))
            throw new RuntimeException("District not in province");
        if (!w.getDistrictCode().equals(d.getCode()))
            throw new RuntimeException("Ward not in district");

        user.setProvinceCode(p.getCode());
        user.setProvinceName(p.getName());

        user.setDistrictCode(d.getCode());
        user.setDistrictName(d.getName());

        user.setWardCode(w.getCode());
        user.setWardName(w.getName());

        userRepository.save(user);

        return new AddressDTO(
                p.getCode(), p.getName(),
                d.getCode(), d.getName(),
                w.getCode(), w.getName()
        );
    }

    //sửa social link
    public Map<String, String> editSocialLink(
            Map<String, String> body,
            String username
    ) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        String socialLink = body.get("socialLink");

        user.setSocialLink(socialLink);
        userRepository.save(user);

        return Map.of(
                "socialLink", socialLink
        );
    }

}

















