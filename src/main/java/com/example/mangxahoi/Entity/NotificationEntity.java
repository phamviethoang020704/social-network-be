package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.NotificationTargetType;
import com.example.mangxahoi.Enums.NotificationType;
import com.example.mangxahoi.Enums.ReactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                // Tối ưu lấy danh sách thông báo của 1 user, sắp xếp theo thời gian
                @Index(name = "idx_notification_receiver_created", columnList = "receiver_id, created_at"),

                // Tối ưu đếm số thông báo chưa đọc
                @Index(name = "idx_notification_receiver_read", columnList = "receiver_id, is_read"),

                // Tối ưu lọc theo loại thông báo
                @Index(name = "idx_notification_type", columnList = "notification_type"),

                // Tối ưu tìm thông báo theo đối tượng liên quan
                @Index(name = "idx_notification_target", columnList = "target_type, target_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    // ID chính của thông báo
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    // Ví dụ: người được like bài viết, người được gửi lời mời kết bạn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    // Người tạo ra hành động
    // Ví dụ: người like, comment, share, gửi kết bạn, mời vào nhóm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private UserEntity actor;

    // Loại thông báo
    // Ví dụ: POST_REACTION, POST_COMMENT, COMMENT_MENTION, FRIEND_REQUEST
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    // Loại đối tượng chính mà thông báo liên quan đến
    // Ví dụ: POST, SHARE, IMAGE, COMMENT, FRIEND, GROUP
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 50)
    private NotificationTargetType targetType;

    // ID chính tương ứng với targetType
    // Nếu targetType = POST thì targetId = postId
    // Nếu targetType = SHARE thì targetId = shareId
    // Nếu targetType = IMAGE thì targetId = imageId
    // Nếu targetType = COMMENT thì targetId = commentId
    // Nếu targetType = FRIEND thì targetId = friendId
    // Nếu targetType = GROUP thì targetId = groupId
    @Column(name = "target_id")
    private Long targetId;

    // ID bài viết gốc
    // Dùng cho thông báo liên quan đến bài viết hoặc ảnh thuộc bài viết
    @Column(name = "post_id")
    private Long postId;

    // ID bài share
    // Dùng cho thông báo like/comment vào bài share
    @Column(name = "share_id")
    private Long shareId;

    // ID ảnh
    // Dùng cho thông báo like/comment ảnh
    @Column(name = "image_id")
    private Long imageId;

    // ID comment cần focus/highlight
    // Dùng cho comment post, comment share, comment image, mention trong bình luận
    @Column(name = "comment_id")
    private Long commentId;

    // ID comment cha
    // Dùng khi commentId là comment con
    // Giúp frontend/backend mở đúng nhánh bình luận cha - con
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    // ID nhóm
    // Dùng cho thông báo được duyệt vào nhóm hoặc được mời vào nhóm
    @Column(name = "group_id")
    private Long groupId;

    // ID bản ghi kết bạn
    // Dùng cho thông báo gửi lời mời kết bạn hoặc chấp nhận kết bạn
    @Column(name = "friend_id")
    private Long friendId;

    // Loại cảm xúc
    // Dùng cho POST_REACTION, SHARE_REACTION, IMAGE_REACTION
    // Ví dụ: LIKE, LOVE, CARE, HAHA, WOW, SAD, ANGRY
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", length = 50)
    private ReactionType reactionType;

    // Đã đọc hay chưa
    // false: chưa đọc
    // true: đã đọc
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    // Đã nhìn thấy trong dropdown/list thông báo hay chưa
    // Có thể dùng để phân biệt với read
    // seen = đã hiện qua mắt người dùng
    // read = người dùng đã bấm hoặc đánh dấu đã đọc
    @Column(name = "is_seen", nullable = false)
    private boolean seen = false;

    // Nội dung thông báo đã dựng sẵn
    // Ví dụ: "Nguyễn Văn A đã thả haha vào bài viết của bạn"
    // Có field này thì frontend hiển thị nhanh, không phải tự ghép text quá nhiều
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // URL frontend cần chuyển đến khi click thông báo
    // Ví dụ:
    // /posts/15
    // /posts/15?commentId=88
    // /shares/8?commentId=99
    // /posts/15/images/3?commentId=120
    // /profile/5
    // /groups/2
    @Column(name = "redirect_url", length = 500)
    private String redirectUrl;

    // Thời gian tạo thông báo
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Thời gian cập nhật thông báo
    // Ví dụ khi chuyển read = true hoặc seen = true
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}