package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.LikeTargetType;
import com.example.mangxahoi.Enums.ReactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "like_target_type", "target_id"}
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    private Long targetId;


    @Enumerated(EnumType.STRING)
    private LikeTargetType likeTargetType;// POST,IMAGE,SHARE,COMMENT

    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;//LIKE,LOVE,CARE,HAHA,WOW,SAD,ANGRY

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
