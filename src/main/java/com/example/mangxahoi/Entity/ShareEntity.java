package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.PostType;
import com.example.mangxahoi.Enums.ShareType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "shares",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_target_type",
                        columnNames = {"user_id","target_id","share_type"}
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caption;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private ShareType shareType; //POST,IMAGE

    private Long targetId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

}

