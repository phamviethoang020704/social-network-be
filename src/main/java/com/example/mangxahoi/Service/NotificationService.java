package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Response.NotificationResponse;
import com.example.mangxahoi.Entity.NotificationEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.CommentTargetType;
import com.example.mangxahoi.Enums.NotificationTargetType;
import com.example.mangxahoi.Enums.NotificationType;
import com.example.mangxahoi.Enums.ReactionType;
import com.example.mangxahoi.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    @Lazy
    private ImageService imageService;

    public void createFriendRequest(UserEntity sender, UserEntity receiver, Long friendId) {
        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(sender)
                .notificationType(NotificationType.FRIEND_REQUEST)
                .targetType(NotificationTargetType.FRIEND)
                .targetId(friendId)
                .friendId(friendId)
                .message(sender.getFullName() + " đã gửi cho bạn lời mời kết bạn")
                .redirectUrl("/profile/" + sender.getId())
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }

    public void createFriendAccepted(UserEntity accepter, UserEntity receiver, Long friendId) {
        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(accepter)
                .notificationType(NotificationType.FRIEND_ACCEPTED)
                .targetType(NotificationTargetType.FRIEND)
                .targetId(friendId)
                .friendId(friendId)
                .message(accepter.getFullName() + " đã chấp nhận lời mời kết bạn của bạn")
                .redirectUrl("/profile/" + accepter.getId())
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }

    public List<NotificationResponse> getMyNotifications(UserEntity receiver) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(receiver)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long countUnread(UserEntity receiver) {
        return notificationRepository.countByReceiverAndReadFalse(receiver);
    }

    private NotificationResponse toResponse(NotificationEntity n) {
        UserEntity actor = n.getActor();

        return new NotificationResponse(
                n.getId(),
                n.getReceiver().getId(),
                actor != null ? actor.getId() : null,
                actor != null ? actor.getFullName() : null,
                actor != null ? imageService.buildImageUrl(actor.getAvatar()) : null,
                n.getNotificationType(),
                n.getTargetType(),
                n.getTargetId(),
                n.getFriendId(),
                n.getMessage(),
                n.getRedirectUrl(),
                n.isRead(),
                n.isSeen(),
                n.getCreatedAt(),
                n.getReactionType(),

                n.getPostId(),
                n.getShareId(),
                n.getImageId(),
                n.getCommentId(),
                n.getParentCommentId()
        );
    }

    public NotificationResponse markAsRead(Long notificationId, UserEntity receiver) {
        NotificationEntity notification = notificationRepository
                .findByIdAndReceiver(notificationId, receiver)
                .orElseThrow(() -> new RuntimeException("notification not found"));

        notification.setRead(true);

        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void deleteFriendRequestNotification(UserEntity sender, UserEntity receiver) {
        notificationRepository.deleteByReceiverAndActorAndNotificationType(
                receiver,
                sender,
                NotificationType.FRIEND_REQUEST
        );
    }

    public void createPostComment(
            UserEntity actor,
            UserEntity receiver,
            Long postId,
            Long commentId
    ) {
        if (actor.getId().equals(receiver.getId())) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(actor)
                .notificationType(NotificationType.POST_COMMENT)
                .targetType(NotificationTargetType.POST)
                .targetId(postId)
                .postId(postId)
                .commentId(commentId)
                .message(actor.getFullName() + " đã bình luận vào bài viết của bạn")
                .redirectUrl("/post/" + postId + "?commentId=" + commentId)
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }

    //UserA đã bình luận vào bài viết mà bạn đã chia sẻ
    public void createShareComment(
            UserEntity actor,
            UserEntity receiver,
            Long shareId,
            Long commentId
    ) {
        if (actor.getId().equals(receiver.getId())) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(actor)
                .notificationType(NotificationType.SHARE_COMMENT)
                .targetType(NotificationTargetType.SHARE)
                .targetId(shareId)
                .shareId(shareId)
                .commentId(commentId)
                .message(actor.getFullName() + " đã bình luận vào bài viết bạn đã chia sẻ")
                .redirectUrl("/share/" + shareId + "?commentId=" + commentId)
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }

    //UserA đã thả cảm xúc vào bài viết của bạn
    public void createPostReaction(
            UserEntity actor,
            UserEntity receiver,
            Long postId,
            ReactionType reactionType
    ) {
        if (actor.getId().equals(receiver.getId())) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(actor)
                .notificationType(NotificationType.POST_REACTION)
                .targetType(NotificationTargetType.POST)
                .targetId(postId)
                .postId(postId)
                .reactionType(reactionType)
                .message(actor.getFullName() + " đã thả cảm xúc vào bài viết của bạn")
                .redirectUrl("/post/" + postId)
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }

    //UserA đã thả cảm xúc vào bài viết bạn đã chia sẻ
    public void createShareReaction(
            UserEntity actor,
            UserEntity receiver,
            Long shareId,
            ReactionType reactionType
    ) {
        if (actor.getId().equals(receiver.getId())) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(actor)
                .notificationType(NotificationType.SHARE_REACTION)
                .targetType(NotificationTargetType.SHARE)
                .targetId(shareId)
                .shareId(shareId)
                .reactionType(reactionType)
                .message(actor.getFullName() + " đã thả cảm xúc vào bài viết bạn đã chia sẻ")
                .redirectUrl("/share/" + shareId)
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }

    //UserA đã chia sẻ bài viết của bạn
    public void createPostShared(
            UserEntity actor,
            UserEntity receiver,
            Long postId,
            Long shareId
    ) {
        if (actor.getId().equals(receiver.getId())) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(actor)
                .notificationType(NotificationType.POST_SHARED)
                .targetType(NotificationTargetType.SHARE)
                .targetId(shareId)
                .postId(postId)
                .shareId(shareId)
                .message(actor.getFullName() + " đã chia sẻ bài viết của bạn")
                .redirectUrl("/share/" + shareId)
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }

    //nhắc đến trong bình luận
    public void createCommentMention(
            UserEntity actor,
            UserEntity receiver,
            CommentTargetType commentTargetType,
            Long targetId,
            Long commentId,
            Long parentCommentId
    ) {
        if (receiver == null) {
            return;
        }

        if (actor.getId().equals(receiver.getId())) {
            return;
        }

        String redirectUrl;

        Long postId = null;
        Long shareId = null;
        Long imageId = null;

        if (commentTargetType == CommentTargetType.POST) {
            postId = targetId;
            redirectUrl = "/post/" + targetId
                    + "?commentId=" + commentId
                    + "&parentCommentId=" + parentCommentId;
        } else if (commentTargetType == CommentTargetType.SHARE) {
            shareId = targetId;
            redirectUrl = "/share/" + targetId
                    + "?commentId=" + commentId
                    + "&parentCommentId=" + parentCommentId;
        } else {
            imageId = targetId;
            redirectUrl = "/post/" + targetId
                    + "?commentId=" + commentId
                    + "&parentCommentId=" + parentCommentId;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .actor(actor)
                .notificationType(NotificationType.COMMENT_MENTION)
                .targetType(NotificationTargetType.COMMENT)
                .targetId(commentId)
                .postId(postId)
                .shareId(shareId)
                .imageId(imageId)
                .commentId(commentId)
                .parentCommentId(parentCommentId)
                .message(actor.getFullName() + " đã nhắc đến bạn trong một bình luận")
                .redirectUrl(redirectUrl)
                .read(false)
                .seen(false)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );
    }
}