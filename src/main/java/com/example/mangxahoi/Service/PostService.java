package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.EditContent;
import com.example.mangxahoi.DTO.Request.PostRequest;
import com.example.mangxahoi.DTO.Response.*;
import com.example.mangxahoi.Entity.*;
import com.example.mangxahoi.Enums.*;
import com.example.mangxahoi.Mapper.ImageMapper;
import com.example.mangxahoi.Mapper.PostMapper;
import com.example.mangxahoi.Mapper.TagMapper;
import com.example.mangxahoi.Repository.*;
import com.example.mangxahoi.Service.Search.UpsertService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Configuration
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final TagRepository tagRepository;
    private final ImageMapper imageMapper;
    private final TagMapper tagMapper;
    private final ImageService imageService;
    private final PostMapper postMapper;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final FeedItemRepository feedItemRepository;
    private final UpsertService searchService;
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.default.avatar.male}")
    private String maleDefaultAvatar;

    @Value("${app.default.avatar.female}")
    private String femaleDefaultAvatar;

    @Value("${app.default.cover-photo}")
    private String coverPhotoDefault;
    @Transactional
    public Object createOrUpdatePost(PostRequest request, List<MultipartFile> files) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isUpdate = request.getPostId() != null;
        PostEntity postEntity;

        if(isUpdate) {
            postEntity = postRepository.findById(request.getPostId()).orElseThrow(() -> new RuntimeException("Post not found"));
            if(!postEntity.getUserEntity().getId().equals(userEntity.getId())) {
                throw new RuntimeException("ngời dùng không có quyền sửa");
            }
        }
        else {
            postEntity = new PostEntity();
            postEntity.setUserEntity(userEntity);
            postEntity.setTypePost(PostType.POST);
        }
        postEntity.setContent(request.getContent());

        //xử lí group
        if (request.getGroupId() != null) {
            GroupEntity groupEntity = groupRepository.findById(request.getGroupId()).orElseThrow();
            postEntity.setGroupEntity(groupEntity);
        }

        // Lưu post trước để có ID
        PostEntity savedPost = postRepository.save(postEntity);

        // Xử lý nhiều ảnh
        if (files != null && !files.isEmpty() && files.stream().allMatch(f -> !f.isEmpty())) {
            if(isUpdate && savedPost.getImageEntity() != null && !savedPost.getImageEntity().isEmpty()) {
                imageRepository.deleteByPostEntityId(savedPost.getId());
                savedPost.getImageEntity().clear();
            }
            String folder = uploadDir + "/posts/";
            Files.createDirectories(Paths.get(folder));

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                    String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
                    String fileName = System.currentTimeMillis() + "_" + original;
                    Path filePath = Paths.get(folder, fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    ImageEntity image = new ImageEntity();
                    image.setImageUrl("/posts/" + fileName);
                    image.setPostEntity(savedPost);
                    savedPost.getImageEntity().add(image);

            }
            savedPost = postRepository.save(savedPost);
        }

        //Xử lí tag
        if(request.getTaggedUserIds() != null){
            tagRepository.deleteByPostEntityId(savedPost.getId());
            savedPost.getTagEntity().clear();
            if (!request.getTaggedUserIds().isEmpty()){
                for (Long taggedUserId : request.getTaggedUserIds()) {
                    TagEntity tagEntity = new TagEntity();
                    UserEntity taggedUsers = userRepository.findById(taggedUserId).orElseThrow(() -> new RuntimeException("User not found"));
                    tagEntity.setUserEntity(taggedUsers);
                    tagEntity.setPostEntity(savedPost);
                    tagRepository.save(tagEntity);
                    savedPost.getTagEntity().add(tagEntity);
                }
            }
        }

        try{
            searchService.upsert(SearchType.POST,savedPost.getId(),savedPost.getContent());
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        PostResponse postResponse = new PostResponse();
        postResponse.setId(savedPost.getId());
        postResponse.setContent(savedPost.getContent());
        postResponse.setPostType(savedPost.getTypePost());
        postResponse.setImages(imageMapper.toResponseList(savedPost.getImageEntity()));
        postResponse.setUpdatedAt(savedPost.getUpdatedAt());
        postResponse.setTags(tagMapper.toResponse(savedPost.getTagEntity()));
        postResponse.setAvatar(imageService.buildImageUrl(savedPost.getUserEntity().getAvatar()));
        postResponse.setFullName(savedPost.getUserEntity().getFullName());
        postResponse.setUserId(savedPost.getUserEntity().getId());

        Long postId = savedPost.getId();

        postResponse.setLiked(likeRepository.existsByUserEntityAndTargetIdAndLikeTargetType(userEntity, postId, LikeTargetType.POST));
        postResponse.setLikeCount(likeRepository.countByLikeTargetTypeAndTargetId(LikeTargetType.POST, postId));
        postResponse.setCommentCount(commentRepository.countByCommentTargetTypeAndTargetId(CommentTargetType.POST, postId));
        postResponse.setShareCount(shareRepository.countByShareTypeAndTargetId(ShareType.POST, postId));

        //Khởi tạo FeedItems
        if (request.getGroupId() == null){
            final Long postIdFinal = savedPost.getId();
            final LocalDateTime now = LocalDateTime.now();
            FeedItemEntity feedItem = feedItemRepository
                    .findByRefIdAndFeedType(postIdFinal, FeedType.POST)
                    .orElseGet(() -> {
                        FeedItemEntity f = new FeedItemEntity();
                        f.setUserEntity(userEntity);
                        f.setFeedType(FeedType.POST);
                        f.setRefId(postIdFinal);
                        return f;
                    });
            feedItem.setUpdatedAt(now);
            FeedItemEntity saveFeed = feedItemRepository.save(feedItem);
            FeedResponse feedResponse = new FeedResponse(
                    saveFeed.getId(),
                    saveFeed.getFeedType(),
                    saveFeed.getUpdatedAt(),
                    postResponse,
                    null
            );
            return feedResponse;
        }

        return  postResponse;
    }

    public PostEntity findById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    //xóa bài viết
    @Transactional
    public void deletePost(Long postId, String username) {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUserEntity().getId().equals(user.getId())) {
            throw new RuntimeException("Người dùng không có quyền xóa");
        }

        // xoá quan hệ trước (nếu chưa cascade)
        likeRepository.deleteByTargetIdAndLikeTargetType(postId, LikeTargetType.POST);
        commentRepository.deleteByTargetIdAndCommentTargetType(postId, CommentTargetType.POST);
        tagRepository.deleteByPostEntityId(postId);
        imageRepository.deleteByPostEntityId(postId);
        //xóa feedItem
        FeedItemEntity feedItemEntity = feedItemRepository.findByRefIdAndFeedType(postId,FeedType.POST).orElseThrow(
                () -> new RuntimeException("Feed Item not found")
        );
        feedItemRepository.delete(feedItemEntity);
        postRepository.delete(post);
    }

    public PostSliceResponse getPostFromGroupSlice(Long groupId, int size, LocalDateTime cursorTime, Long cursorId) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<PostEntity> rows;

        if (cursorTime == null || cursorId == null) {
            rows = postRepository.findFirstGroupPostSlice(groupId, pageable);
        } else {
            rows = postRepository.findGroupPostSliceAfterCursor(groupId, cursorTime, cursorId, pageable);
        }

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        // map -> response
        List<PostResponse> items = rows.stream().map(postMapper::toResponse).toList();

        // next cursor
        LocalDateTime nextTime = null;
        Long nextId = null;
        if (!rows.isEmpty()) {
            PostEntity last = rows.get(rows.size() - 1);
            nextTime = last.getUpdatedAt();
            nextId = last.getId();
        }

        return new PostSliceResponse(items, hasNext, nextTime, nextId);
    }

    //sửa content của avatar hoặc coverPhoto
    public EditContent editContent(EditContent request, String username){
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PostEntity post = postRepository.findById(request.id()).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUserEntity().getId().equals(userEntity.getId())) {
            throw new RuntimeException("không có quyền sửa");
        }
        post.setContent(request.content());
        postRepository.save(post);
        return new EditContent(
                request.id(),
                request.content(),
                request.postType()
        );
    }

    public PostResponse getPostByPostId(Long postId, String username){
        userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return postMapper.toResponse(post);

    }
    //xóa avatar hoặc coverPhoto
    @Transactional
    public void deletePostProfile(EditContent request, String username){
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PostEntity post = postRepository.findById(request.id()).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUserEntity().getId().equals(user.getId())) {
            throw new RuntimeException("người dùng không có quyền xóa");
        }
        //xóa like vào post gốc
        likeRepository.deleteAllByTargetIdAndLikeTargetType(post.getId(), LikeTargetType.POST);

        //xóa like vào comment của post gốc
        List<Long> postCommentIds = commentRepository.getCommentIdsByTargetAndType(List.of(post.getId()), CommentTargetType.POST);
        likeRepository.deleteByTargetIdsAndLikeTargetType(
                postCommentIds,
                LikeTargetType.COMMENT
        );

        //xóa comment của post gốc
        commentRepository.deleteAllByTargetIdAndCommentTargetType(post.getId(), CommentTargetType.POST);
        //xóa feed
        feedItemRepository.deleteByRefIdAndFeedType(post.getId(), FeedType.POST);

        //xóa những bài share liên quan nếu có
        List<ShareEntity> shares = shareRepository.findAllByTargetIdAndShareType(request.id(), ShareType.POST).orElse(null);
        if (shares != null && !shares.isEmpty()) {
            //xóa like
            deleteLike(shares);

            //xóa comment
            deleteComment(shares);

            deleteFeeds(shares);
            shareRepository.deleteAll(shares);

        }

        //kiểm tra xóa avt hay coverPhto
        PostType postType = post.getTypePost();
        if (postType == PostType.AVATAR){
            if (user.getGender() == GenderUser.MALE){
                user.setAvatar(maleDefaultAvatar);
                user.setAvatarPostId(null);
            }
            else {
                user.setAvatar(femaleDefaultAvatar);
                user.setCoverPostId(null);
            }
        } else if (postType == PostType.COVER_PHOTO) {
            user.setCoverPhoto(coverPhotoDefault);
        }

        userRepository.save(user);
        postRepository.delete(post);
    }

    //xóa những bài feed share liên quan đến avt, coverPhoto
    private void deleteFeeds(List<ShareEntity> shares) {
        List<Long> shareIds = shares.stream().map(ShareEntity::getId).toList();
        List<FeedItemEntity> feedItemEntities = feedItemRepository.findAllFeedByRefId(shareIds,FeedType.SHARE);
        feedItemRepository.deleteAll(feedItemEntities);
    }
    //xóa những like vào bài share
    private void deleteLike(List<ShareEntity> shares) {
        List<Long> shareIds = shares.stream().map(ShareEntity::getId).toList();
        likeRepository.deleteByTargetIdsAndLikeTargetType(shareIds, LikeTargetType.SHARE);
    }
    //xóa comment vào bài share và xóa like vào comment
    private void deleteComment(List<ShareEntity> shares) {
        List<Long> shareIds = shares.stream().map(ShareEntity::getId).toList();

        List<Long> commentIds = commentRepository.getCommentIdsByTargetAndType(shareIds, CommentTargetType.SHARE);
        likeRepository.deleteByTargetIdsAndLikeTargetType(commentIds, LikeTargetType.COMMENT);

        commentRepository.deleteByTargetIdsAndType(shareIds, CommentTargetType.SHARE);
    }


}
