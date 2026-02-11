package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.FriendStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_send")
    private UserEntity  userSend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_accept")
    private UserEntity  userAccept;

    @Enumerated(EnumType.STRING)
    FriendStatus friendStatus;
    private boolean isAccept;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
