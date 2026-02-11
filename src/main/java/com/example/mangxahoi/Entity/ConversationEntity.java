package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.MessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id","user2_id"}),
        indexes = {
            @Index(name = "idx_conversation_last_message_at", columnList = "lastMessageAt")
        }
)
@Getter
@Setter
public class ConversationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private UserEntity user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private UserEntity user2;

    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private Long lastSenderId;

    @Enumerated(EnumType.STRING)
    private MessageType LastMessageType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
