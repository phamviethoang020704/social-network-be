package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.GroupJoiningStatus;
import com.example.mangxahoi.Enums.PostType;
import com.example.mangxahoi.Enums.RoleGroup;
import com.example.mangxahoi.Enums.TypeJoinGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_member")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name")
    private RoleGroup roleName;//admin,user,owner

    // PENDING_RESPONSE,   // khi người dùng được mời vào nhóm và đang chờ phản hồi
    //PENDING_APPROVAL, //khi người dùng xin vào nhóm và đang đợi duyệt
    //ACCEPTED,  // đã vào group
    @Enumerated(EnumType.STRING)
    @Column(name = "joining_status", nullable = false)
    private GroupJoiningStatus groupJoiningStatus;



    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupEntity groupEntity;

    //lưu người tạo nhóm
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;
    //lưu người mời vào nhóm (null nếu người dùng xin vào)
    @ManyToOne
    @JoinColumn(name = "invited_by")
    private UserEntity invitedBy;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
