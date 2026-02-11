package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.RoleGroup;
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
@Table(name = "groupmxh")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupName;
    private String description;
    private String coverPhoto;
    private boolean isPublic;//nhóm riêng tư hay công khai
    private boolean requireJoinApproval;//bật phê duyệt user xin vào
    private boolean requirePostApproval; //phê duyệt dang bai

    @OneToMany(mappedBy = "groupEntity")
    private Set<PostEntity> postEntity = new HashSet<>();

    @OneToMany(mappedBy = "groupEntity")
    private Set<GroupMemberEntity>  groupMemberEntity = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
