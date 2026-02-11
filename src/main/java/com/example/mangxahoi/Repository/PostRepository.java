package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.Response.PosterInfoDTO;
import com.example.mangxahoi.Entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @Query("""
       select distinct p
       from PostEntity p
       left join fetch p.imageEntity
       left join fetch p.tagEntity
       where p.userEntity.id = :userId
       ORDER BY p.updatedAt desc
""")
    List<PostEntity> findUserPosts(Long userId);

    //lấy ra list content theo postIds
    @Query("""
        select p.id,p.content
        from PostEntity p
        where p.id in :postIds
""")
    List<Object[]> getContentByPostIds(@Param("postIds") List<Long> postIds);

    List<PostEntity> findByUserEntityId(Long userEntityId);

    //lấy ra tất cả post của user
    @Query("""
    select p
    from PostEntity p
    where p.groupEntity is null and p.id in :postIds
""")
    List<PostEntity> ListPostByIds(@Param("postIds") List<Long> postIds);

    //lấy ra tất cả post theo groupId
    @Query("""
        select p from PostEntity p
        where p.groupEntity.id = :groupId
          and (
               :cursorTime is null
               or p.updatedAt < :cursorTime
               or (p.updatedAt = :cursorTime and p.id < :cursorId)
          )
        order by p.updatedAt desc, p.id desc
    """)
    List<PostEntity> findGroupPostSlice(
            @Param("groupId") Long groupId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // lấy ra thông tin người đăng bài của bài chia sẻ
    @Query("""
        select new com.example.mangxahoi.DTO.Response.PosterInfoDTO(
            p.id,
            
            u.id,
            u.fullName,
            u.avatar,
            p.updatedAt
        )
        from PostEntity p
        join p.userEntity u
        where p.id in :postIds
        
""")
    List<PosterInfoDTO> findPosterInfoByPostIds(@Param("postIds") List<Long> postIds);












}
