package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.Response.FriendResponse;
import com.example.mangxahoi.Entity.FriendEntity;
import com.example.mangxahoi.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<FriendEntity, Long> {

    Optional<FriendEntity> findByUserSendAndUserAccept(UserEntity userSend, UserEntity userAccept);

    Optional<FriendEntity> findByUserSendAndUserAcceptOrUserSendAndUserAccept(
            UserEntity userSend1, UserEntity userAccept1,
            UserEntity userSend2, UserEntity userAccept2
    );

    //tính số bb theo userId
    @Query("""
        select count(f)
        from FriendEntity f
        where f.isAccept = true
        and (f.userSend.id = :userId or f.userAccept.id = :userId)
""")
    Long countFriendsByUserId(@Param("userId") Long userId);

    //Kiểm tra bạn bè hợp lệ
    @Query("""
        select f from FriendEntity f 
        where f.isAccept = true
        and (
            (f.userSend.id = :userId and f.userAccept.id in :friendIds)
            or
            (f.userAccept.id = :userId and f.userSend.id in :friendIds)
        )
""")
    List<FriendEntity> findAcceptedFriends(Long userId,List<Long> friendIds);

    //tìm user đã kết bạn theo keyword
    @Query("""
        select f from FriendEntity f
        where f.isAccept = true
        and
        (
            (f.userSend.id = :userId and lower(f.userAccept.fullName) like lower(concat('%', :keyword, '%')))
            or
            (f.userAccept.id = :userId and lower(f.userSend.fullName) like lower(concat('%', :keyword, '%')))
        )
""")
    List<FriendEntity> searchAcceptedFriendsByFullName(
            @Param("userId") Long userId,
            @Param("keyword") String keyword
    );

    //lấy ra danh sách bạn bè theo userId mới kết bạn nhất
    @Query("""
    select f
    from FriendEntity f
    where f.isAccept = true
      and (f.userSend.id = :userId or f.userAccept.id = :userId)
    order by f.createdAt desc
""")
    List<FriendEntity> findTop9RecentFriends(
            @Param("userId") Long userId,
            Pageable pageable
    );

    //vẫn lấy ra danh sách bạn bè nhung dung page va tra ve response
    @Query("""
    select new com.example.mangxahoi.DTO.Response.FriendResponse(
        f.id,
        case
            when f.userAccept.id = :userId then f.userSend.fullName else f.userAccept.fullName
        end,
        case
            when f.userAccept.id = :userId then f.userSend.avatar else f.userAccept.avatar
        end 
    )
    from FriendEntity f 
    where f.isAccept = true
    and(
        f.userSend.id = :userId or f.userAccept.id = :userId
    )
""")
    Page<FriendResponse> getAllFriends(
            @Param("userId") Long userId,
            Pageable pageable
    );

    //lấy ra danh sách bạn chung
    @Query("""
    select new com.example.mangxahoi.DTO.Response.FriendResponse(
        u.id,
        u.fullName,
        u.avatar
    )
    from UserEntity u
    where u.id in (
        select 
            case 
                 when f1.userAccept.id = :viewerId then f1.userSend.id
                 else f1.userAccept.id
            end 
        from FriendEntity f1
        where f1.isAccept = true
        and (f1.userSend.id = :viewerId or f1.userAccept.id = :viewerId)
    )
    and
    u.id in(
        select 
            case 
                when f2.userAccept.id = :ownerId then f2.userSend.id else f2.userAccept.id
            end 
        from FriendEntity f2
        where f2.isAccept = true
        and (f2.userAccept.id = :ownerId or f2.userSend.id = :ownerId)
    )
""")
    List<FriendResponse> getMutualFriends(
            @Param("ownerId") Long ownerId, //người được xem
            @Param("viewerId") Long viewerId //người đăng nhập đang xem
    );

    //lấy ra list id bạn bè
    @Query("""
        select case
            when f.userSend.id = :userId then f.userAccept.id
            else f.userSend.id
        end
        from FriendEntity f
        where f.isAccept = true
          and (f.userSend.id = :userId or f.userAccept.id = :userId)
    """)
    List<Long> findFriendUserIds(@Param("userId") Long userId);
}
















