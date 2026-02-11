package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.Response.GroupRes.MemberInfo;
import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.Entity.GroupMemberEntity;
import com.example.mangxahoi.Enums.GroupJoiningStatus;
import com.example.mangxahoi.Enums.RoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
    boolean existsByGroupEntityIdAndUserEntityId(Long groupId, Long userId);
    Optional<GroupMemberEntity> findByGroupEntityIdAndUserEntityId(Long groupId, Long userId);
    Long countByGroupEntityIdAndGroupJoiningStatus(Long groupId, GroupJoiningStatus groupJoiningStatus);
    boolean existsByUserEntityIdAndGroupEntityIdAndGroupJoiningStatus(Long userId, Long groupId, GroupJoiningStatus groupJoiningStatus);

    //lấy ra thông tin thành viên trong group
    @Query("""
    select new com.example.mangxahoi.DTO.Response.GroupRes.MemberInfo(
        g.userEntity.id,
        g.userEntity.fullName,
        g.userEntity.avatar,
        g.roleName
    )
    from GroupMemberEntity g
    where g.groupEntity.id = :groupId and g.roleName = :roleGroup
""")
    List<MemberInfo> findAllGroupMembers(Long groupId, RoleGroup roleGroup);

    //lay ra het anh trong group
    @Query("""
    select new com.example.mangxahoi.DTO.Response.ImageResponse(
        i.id,
        i.imageUrl
    )
    from ImageEntity i
    join i.postEntity p
    where p.groupEntity.id = :groupId
""")
    List<ImageResponse> getImageByGroupId(Long groupId);

    //kiểm tra xem người dùng đã vào nhóm chưa
    boolean existsByUserEntityIdAndGroupJoiningStatus(Long userId, GroupJoiningStatus groupJoiningStatus);

    Optional<GroupMemberEntity> findByUserEntityIdAndGroupEntityId(Long userId, Long groupId);

    boolean existsByUserEntityIdAndGroupEntityIdAndRoleName(Long userId, Long groupId, RoleGroup roleName);

    //lay ra danh sách người xin vào nhóm
    @Query("""
        select new com.example.mangxahoi.DTO.Response.GroupRes.MemberInfo
        (
            g.userEntity.id,
            g.userEntity.fullName,
            g.userEntity.avatar,
            null
        )
        from GroupMemberEntity g
        where g.groupEntity.id = :groupId and g.groupJoiningStatus = :groupJoiningStatus
""")
    List<MemberInfo> getListMemberRequest(Long groupId,GroupJoiningStatus groupJoiningStatus);

    List<GroupMemberEntity> findAllByGroupEntityIdAndGroupJoiningStatus(Long groupEntityId, GroupJoiningStatus groupJoiningStatus);

    //lấy ra list id nhóm đã vào
    @Query("""
        select gm.groupEntity.id
        from GroupMemberEntity gm
        where gm.userEntity.id = :userId
          and gm.groupJoiningStatus = com.example.mangxahoi.Enums.GroupJoiningStatus.ACCEPTED
    """)
    List<Long> findJoinedGroupIds(@Param("userId") Long userId);

    @Query("""
        select gm.groupEntity.id, gm.groupJoiningStatus
        from GroupMemberEntity gm
        where gm.userEntity.id = :userId
          and gm.groupEntity.id in :groupIds
    """)
    List<Object[]> findStatusByUserAndGroupIds(@Param("userId") Long userId,
                                               @Param("groupIds") List<Long> groupIds);

}
