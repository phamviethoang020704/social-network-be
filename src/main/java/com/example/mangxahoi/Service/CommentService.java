package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Request.CommentRequest;
import com.example.mangxahoi.DTO.Response.CommentResponse;
import com.example.mangxahoi.DTO.Response.DeleteCommentResponse;
import com.example.mangxahoi.Entity.*;
import com.example.mangxahoi.Enums.CommentTargetType;
import com.example.mangxahoi.Enums.LikeTargetType;
import com.example.mangxahoi.Enums.ReactionType;
import com.example.mangxahoi.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final LikeRepository likeRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final LikeService likeService;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final ShareRepository shareRepository;
    @Value("${app.upload.dir}")
    private String uploadDir;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public CommentService(UserRepository userRepository, CommentRepository commentRepository, LikeRepository likeRepository, ImageRepository imageRepository, ImageService imageService, LikeService likeService, PostRepository postRepository, NotificationService notificationService, ShareRepository shareRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
        this.likeService = likeService;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
        this.shareRepository = shareRepository;
    }
    //tạo hoặc sửa comment
    public CommentResponse createOrUpdateComment(CommentRequest request, MultipartFile image, String username) throws IOException {
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("user not found")
        );
        boolean isUpdate = request.commentId() != null;
        CommentEntity commentEntity;

        if(isUpdate){
            commentEntity = commentRepository.findById(request.commentId()).orElseThrow(()-> new RuntimeException("comment not found"));
            if(!commentEntity.getUserEntity().getId().equals(userEntity.getId())){
                throw new RuntimeException("Người dùng không có quyền sửa");
            }
        }
        else {
            commentEntity = new CommentEntity();
            commentEntity.setUserEntity(userEntity);
            commentEntity.setCommentTargetType(request.commentTargetType());
            commentEntity.setTargetId(request.targetId());
        }
        commentEntity.setContent(request.content());

        var targetType = commentEntity.getCommentTargetType();
        var targetId = commentEntity.getTargetId();

        if(image != null && !image.isEmpty()){
            if (commentEntity.getImageUrl() != null) {
                String oldRel = commentEntity.getImageUrl().startsWith("/")
                        ? commentEntity.getImageUrl().substring(1)
                        : commentEntity.getImageUrl();
                Path oldFile = Paths.get(uploadDir, oldRel);
                Files.deleteIfExists(oldFile);
            }
            String folder = Paths.get(uploadDir, "comments").toString();
            Files.createDirectories(Paths.get(folder));
            String original = Paths.get(image.getOriginalFilename()).getFileName().toString();
            String fileName = UUID.randomUUID() + "_" + original;
            Path filePath = Paths.get(folder, fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            commentEntity.setImageUrl("/comments/" + fileName);
        }


        if (request.replyId() != null) {
            UserEntity userReceivedReply = userRepository.findById(request.replyId()).orElseThrow(()-> new RuntimeException("người được rep không tồn tại"));
            commentEntity.setReplyId(userReceivedReply);
        }

        if (request.parentId() != null) {
            CommentEntity parent = commentRepository.findById(request.parentId()).orElseThrow(
                    () -> new RuntimeException("parent not found")
            );
            if(!parent.getTargetId().equals(request.targetId()) ||
                parent.getCommentTargetType() != request.commentTargetType()) {
                throw new IllegalArgumentException("Parent comment does not belong to the same target");
            }
            commentEntity.setParent(parent);
        }
        CommentEntity saveCmt = commentRepository.save(commentEntity);
        // Thông báo khi bình luận vào bài viết
        if (
                !isUpdate
                        && request.parentId() == null
                        && request.commentTargetType() == CommentTargetType.POST
        ) {
            PostEntity post = postRepository.findById(request.targetId())
                    .orElseThrow(() -> new RuntimeException("post not found"));

            notificationService.createPostComment(
                    userEntity,
                    post.getUserEntity(),
                    post.getId(),
                    saveCmt.getId()
            );
        }

        // Thông báo khi bình luận vào bài share
        if (
                !isUpdate
                        && request.parentId() == null
                        && request.commentTargetType() == CommentTargetType.SHARE
        ) {
            ShareEntity share = shareRepository.findById(request.targetId())
                    .orElseThrow(() -> new RuntimeException("share not found"));

            notificationService.createShareComment(
                    userEntity,
                    share.getUserEntity(),
                    share.getId(),
                    saveCmt.getId()
            );
        }

        // Thông báo khi trả lời / nhắc đến người khác trong bình luận
        if (
                !isUpdate
                        && saveCmt.getParent() != null
                        && saveCmt.getReplyId() != null
        ) {
            notificationService.createCommentMention(
                    userEntity,
                    saveCmt.getReplyId(),
                    saveCmt.getCommentTargetType(),
                    saveCmt.getTargetId(),
                    saveCmt.getId(),
                    saveCmt.getParent().getId()
            );
        }
        //trả về
        Long countLike = likeRepository.countByLikeTargetTypeAndTargetId(LikeTargetType.COMMENT, saveCmt.getId());
        boolean isLiked = likeRepository.existsByUserEntityAndTargetIdAndLikeTargetType(userEntity, saveCmt.getId(), LikeTargetType.COMMENT);

        ReactionType reactionType = null;
        if (isLiked) {
            reactionType = likeRepository.findByUserEntityAndLikeTargetTypeAndTargetId(userEntity, LikeTargetType.COMMENT, saveCmt.getId())
                    .orElseThrow()
                    .getReactionType();
        }

        Long countComment = commentRepository.countByCommentTargetTypeAndTargetId(targetType, targetId);
        return new CommentResponse(
                saveCmt.getId(),
                saveCmt.getContent(),
                imageService.buildImageUrl(saveCmt.getImageUrl()),
                imageService.buildImageUrl(saveCmt.getUserEntity().getAvatar()),
                saveCmt.getUserEntity().getFullName(),
                countComment,
                countLike,
                saveCmt.getUpdatedAt(),
                commentRepository.countByParentId(saveCmt.getId()),
                saveCmt.getParent() != null ? saveCmt.getParent().getId() : null,
                isLiked,
                reactionType,
                likeService.getFullReaction(saveCmt.getId(),LikeTargetType.COMMENT),
                saveCmt.getReplyId() != null ? saveCmt.getReplyId().getFullName() : null,
                saveCmt.getTargetId(),
                saveCmt.getUserEntity().getId(),
                userEntity.getId()
        );
    }

    // Lấy ra các cmt cha
    public List<CommentResponse> getComments(CommentTargetType commentTargetType, Long targetId, int page,String username) {
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("user not found"));
        Pageable pageable = PageRequest.of(page, 10);
        Page<CommentEntity> pageEntity = commentRepository.findRootComments(targetId,commentTargetType,pageable);
        List<CommentEntity> comments = pageEntity.getContent();

        if (comments.isEmpty()) {
            return List.of();
        }
        List<Long> commentIds = comments.stream().map(CommentEntity::getId).toList();

        //chuyển object id,countLike thành map
        Map<Long,Long> likeMap = likeRepository.countLikes(LikeTargetType.COMMENT,commentIds)
                .stream()
                .collect(Collectors.toMap(
                   r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        //chuyển object id,countReply thành map
        Map<Long,Long> replyMap = commentRepository.countReplies(commentIds)
                .stream()
                .collect(Collectors.toMap(
                        r-> (Long) r[0],
                                r -> (Long) r[1]
                ));

        //
        List<Object[]> reactions = likeRepository.existsReaction(
                userEntity.getId(),
                LikeTargetType.COMMENT,
                commentIds
        );
        Map<Long, ReactionType> reactionMap = new HashMap<>();
        for (Object[] r: reactions){
            Long tarId = (Long) r[0];
            ReactionType reactionType = (ReactionType) r[1];
            reactionMap.put(tarId, reactionType);
        }
        return comments.stream()
                .map(c -> {
                    ReactionType rt = reactionMap.get(c.getId());
                    return new CommentResponse(
                        c.getId(),
                        c.getContent(),
                        imageService.buildImageUrl(c.getImageUrl()),
                        imageService.buildImageUrl(c.getUserEntity().getAvatar()),
                        c.getUserEntity().getFullName(),
                        null,
                        likeMap.getOrDefault(c.getId(), 0L),
                        c.getUpdatedAt(),
                        replyMap.getOrDefault(c.getId(), 0L),
                        null,
                        rt != null,
                            rt,
                            likeService.getFullReaction(c.getId(),LikeTargetType.COMMENT),
                            null,
                            targetId,
                            c.getUserEntity().getId(),
                            userEntity.getId()
                    );
                }).toList();
    }

    // lấy ra các cmt con
    public List<CommentResponse> getReplies(Long parentId,int page,String username) {
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("user not found"));
        CommentEntity cE = commentRepository.findById(parentId).orElseThrow(()->new RuntimeException("parent not found"));

        Pageable pageable = PageRequest.of(page, 10);
        Page<CommentEntity> pageEntity = commentRepository.getReplies(parentId,pageable);
        List<CommentEntity> comments = pageEntity.getContent();
        if (comments.isEmpty()) {
            return List.of();
        }
        List<Long> commentIds = comments.stream().map(CommentEntity::getId).toList();
        Map<Long, Long> likeMap = likeRepository.countLikes(LikeTargetType.COMMENT,commentIds)
                .stream().collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        //
        Map<Long,Long> replyMap = commentRepository.countReplies(commentIds)
                .stream().collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        //
        List<Object[]> reactions = likeRepository.existsReaction(
                userEntity.getId(),
                LikeTargetType.COMMENT,
                commentIds
        );
        Map<Long, ReactionType> reactionMap = new HashMap<>();
        for (Object[] r: reactions){
            Long tarId = (Long) r[0];
            ReactionType reactionType = (ReactionType) r[1];
            reactionMap.put(tarId, reactionType);
        }
        return comments.stream().map(c -> {
            ReactionType rt = reactionMap.get(c.getId());
            String nameUserReceivedReply = null;
            if (c.getReplyId() != null) {
                nameUserReceivedReply =
                        c.getReplyId()
                                .getFullName();
            }
            CommentResponse commentResponse = new CommentResponse(
                    c.getId(),
                    c.getContent(),
                    imageService.buildImageUrl(c.getImageUrl()),
                    imageService.buildImageUrl(c.getUserEntity().getAvatar()),
                    c.getUserEntity().getFullName(),
                    null,
                    likeMap.getOrDefault(c.getId(),0L),
                    c.getUpdatedAt(),
                    replyMap.getOrDefault(c.getId(),0L),
                    parentId,
                    rt != null,
                    rt,
                    likeService.getFullReaction(c.getId(),LikeTargetType.COMMENT),
                    nameUserReceivedReply,
                    cE.getTargetId(),
                    c.getUserEntity().getId(),
                    userEntity.getId()
            );
            return commentResponse;
        }).toList();

    }

    // xóa comment
    @Transactional
    public DeleteCommentResponse deleteComment(Long commentId, String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("user not found"));
        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(()->new RuntimeException("comment not found"));

        if(!comment.getUserEntity().getId().equals(user.getId())){
            throw new RuntimeException("người dùng không có quyền xóa");
        }

        CommentTargetType targetType = comment.getCommentTargetType();
        Long targetId = comment.getTargetId();
        Long parentId = comment.getParent() != null ?comment.getParent().getId() : null;

        List<String> imageUrlsToDelete = new ArrayList<>();
        if(comment.getImageUrl() != null && !comment.getImageUrl().isBlank()){
            imageUrlsToDelete.add(comment.getImageUrl());
        }
        Long deleteCount;
        //nếu là con-> xóa duy nhất con
        if(comment.getParent() != null){
            likeRepository.deleteByTargetIdAndLikeTargetType(commentId, LikeTargetType.COMMENT);
            commentRepository.delete(comment);
            deleteCount = 1L;
        }
        //nếu là cha-> xóa hết cha con
        else {
            List<Long> replyIds = commentRepository.findReplyIdsByParentId(commentId);
            imageUrlsToDelete.addAll(commentRepository.findReplyImageUrlsByParentId(commentId));
            //xóa hết like thằng con
            if(!replyIds.isEmpty()){
                likeRepository.deleteByTargetIdsAndLikeTargetType(replyIds, LikeTargetType.COMMENT);
            }
            //xóa like cha
            likeRepository.deleteByTargetIdAndLikeTargetType(commentId, LikeTargetType.COMMENT);
            int repliesDeleted = commentRepository.deleteAllByParentId(commentId);
            commentRepository.delete(comment);
            deleteCount = 1L + repliesDeleted;
        }
        Long countCommentRoot = commentRepository.countByCommentTargetTypeAndTargetId(targetType, comment.getTargetId());
        deleteFilesAfterCommit(imageUrlsToDelete);
        return new DeleteCommentResponse(
                commentId,
                parentId,
                targetId,
                targetType,
                deleteCount,
                countCommentRoot
        );
    }

    //xóa các ảnh của comment
    private void deleteFilesAfterCommit(List<String> relUrls){
        if(relUrls == null || relUrls.isEmpty())
            return;
        List<String> files = new ArrayList<>(relUrls);
        if(TransactionSynchronizationManager.isSynchronizationActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
                @Override
                public void afterCommit() {
                    for (String url : files) {
                        try {
                            String rel = url.startsWith("/") ? url.substring(1) : url;
                            Path file = Paths.get(uploadDir, rel);
                            Files.deleteIfExists(file);
                        }
                        catch (Exception e){}
                    }
                }
            });
        }
        else {
            for (String url : files) {
                try{
                    String rel = url.startsWith("/") ? url.substring(1) : url;
                    Path file = Paths.get(uploadDir, rel);
                    Files.deleteIfExists(file);
                }
                catch (Exception ignored) {}
            }
        }
    }

    public CommentResponse getCommentById(Long commentId, String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found"));

        CommentEntity c = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("comment not found"));

        Long likeCount = likeRepository.countByLikeTargetTypeAndTargetId(
                LikeTargetType.COMMENT,
                c.getId()
        );

        boolean isLiked = likeRepository.existsByUserEntityAndTargetIdAndLikeTargetType(
                userEntity,
                c.getId(),
                LikeTargetType.COMMENT
        );

        ReactionType reactionType = null;

        if (isLiked) {
            reactionType = likeRepository
                    .findByUserEntityAndLikeTargetTypeAndTargetId(
                            userEntity,
                            LikeTargetType.COMMENT,
                            c.getId()
                    )
                    .orElseThrow()
                    .getReactionType();
        }

        return new CommentResponse(
                c.getId(),
                c.getContent(),
                imageService.buildImageUrl(c.getImageUrl()),
                imageService.buildImageUrl(c.getUserEntity().getAvatar()),
                c.getUserEntity().getFullName(),
                null,
                likeCount,
                c.getUpdatedAt(),
                commentRepository.countByParentId(c.getId()),
                c.getParent() != null ? c.getParent().getId() : null,
                isLiked,
                reactionType,
                likeService.getFullReaction(c.getId(), LikeTargetType.COMMENT),
                c.getReplyId() != null ? c.getReplyId().getFullName() : null,
                c.getTargetId(),
                c.getUserEntity().getId(),
                userEntity.getId()
        );
    }
}
