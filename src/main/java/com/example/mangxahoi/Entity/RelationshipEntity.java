package com.example.mangxahoi.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table(name = "relationships")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "code_relationship")
    private String codeRelationship;

    @OneToMany(mappedBy = "relationshipEntity")
    private Set<UserEntity> userEntity =  new HashSet<>();


}
