package com.example.mangxahoi.Entity;

import com.example.mangxahoi.Enums.GenderUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "avatar_post_id")
    private Long avatarPostId;

    @Column(name = "cover_post_id")
    private Long coverPostId;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "cover_Photo")
    private String coverPhoto;

    @Column(name = "biography")
    private String biography;
    //tỉnh
    private Integer provinceCode;
    private String provinceName;
    //huyện
    private Integer districtCode;
    private String districtName;
    // xã
    private Integer wardCode;
    private String wardName;
    //địa chỉ chi tiết
    private String addressDetail;

    private String phoneNumber;
    @Column(name = "high_school")
    private String highSchool;

    @Column(name = "university")
    private String university;

    @Column(name = "work")
    private String work;

    @Column(name = "social_link")
    private String socialLink;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "language")
    private String language;

    @Column(name = "introduce")
    private String introduce;
    private boolean isRelationship;
    private int relationshipUserId;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "relationship_id")
    private RelationshipEntity relationshipEntity;

    @OneToMany(mappedBy = "userEntity")
    private Set<PostEntity> postEntity = new HashSet<>();

    @OneToMany(mappedBy = "userEntity")
    private Set<LikeEntity> likeEntity =  new HashSet<>();

    @OneToMany(mappedBy = "userEntity")
    private Set<CommentEntity> commentEntity =  new HashSet<>();


    @OneToMany(mappedBy = "userEntity")
    private Set<GroupMemberEntity> groupMemberEntity = new HashSet<>();

    @OneToMany(mappedBy = "userEntity")
    private Set<ShareEntity> shareEntity = new HashSet<>();

    @OneToMany(mappedBy = "userEntity")
    private Set<GroupEntity> groupEntity = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity roleEntity;

    @OneToMany(mappedBy = "userSend", cascade = CascadeType.ALL)
    private List<FriendEntity> friendRequestsSent = new ArrayList<>();

    @OneToMany(mappedBy = "userAccept", cascade = CascadeType.ALL)
    private List<FriendEntity> friendRequestsReceived = new ArrayList<>();

    @OneToMany(mappedBy = "userEntity")
    private List<TagEntity> tagEntity = new ArrayList<>();

    @OneToMany(mappedBy = "replyId")
    private List<CommentEntity> commentEntities = new ArrayList<>();

    @OneToMany(mappedBy = "userEntity")
    private List<FeedItemEntity> feedItemEntities = new ArrayList<>();

    @OneToMany(mappedBy = "userEntity")
    private List<RecentSearchEntity> recentSearchEntities = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private GenderUser gender;
}
