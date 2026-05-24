package com.example.mangxahoi.Enums;

public enum NotificationType {
    FRIEND_REQUEST,          // UserA đã gửi cho bạn lời mời kết bạn
    FRIEND_ACCEPTED,         // UserA đã chấp nhận lời mời kết bạn của bạn

    POST_COMMENT,            // UserA đã bình luận vào bài viết của bạn
    POST_REACTION,           // UserA đã thả cảm xúc vào bài viết của bạn
    POST_SHARED,             // UserA đã chia sẻ bài viết của bạn

    SHARE_COMMENT,           // UserA đã bình luận vào bài viết bạn đã chia sẻ
    SHARE_REACTION,          // UserA đã thả cảm xúc vào bài viết bạn đã chia sẻ

    IMAGE_COMMENT,
    IMAGE_REACTION,

    COMMENT_MENTION,         // UserA đã nhắc đến bạn trong một bình luận

    GROUP_JOIN_APPROVED,     // Quản trị viên đã duyệt bạn vào nhóm
    GROUP_INVITED            // Bạn được UserA mời vào nhóm
}