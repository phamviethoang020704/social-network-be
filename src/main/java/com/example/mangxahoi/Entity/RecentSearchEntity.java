package com.example.mangxahoi.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "recent_searches",

        uniqueConstraints = @UniqueConstraint(
                name = "ux_user_keyword",
                columnNames = {"user_id", "keyword"}
        ),

        indexes = {
                @Index(
                        name = "idx_user_last_used",
                        columnList = "user_id, last_used_at"
                )
        }
)
public class RecentSearchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(nullable = false, length = 255)
    private String keyword;

    private LocalDateTime lastUsedAt;
}
