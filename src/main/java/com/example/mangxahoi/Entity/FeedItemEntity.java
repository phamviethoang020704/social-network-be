package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.FeedType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "feed_item", indexes = {
        @Index(name="idx_feed_user_updated", columnList="user_id, updated_at, id")
})
public class FeedItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name="feed_type", nullable=false)
    private FeedType feedType; // POST | SHARE

    @Column(name="ref_id", nullable=false)
    private Long refId; // postId OR shareId

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
}
