package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.Response.ImageResponse;
import com.example.mangxahoi.DTO.Response.PosterInfoDTO;
import com.example.mangxahoi.Entity.ImageEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByPostEntity(PostEntity postEntity);
    List<ImageEntity> findTop9ByPostEntity_UserEntityAndPostEntity_GroupEntityIsNullOrderByCreatedAtDesc(UserEntity userEntity);
    List<ImageEntity> findByPostEntityIdOrderByIdAsc(Long postId);

    void deleteByPostEntityId(Long postEntityId);

    //lấy list image theo list postid
    @Query("""
        select i.postEntity.id, i.id, i.imageUrl
        from ImageEntity i
        where i.postEntity.id in :ids
""")
    List<Object[]> getImagesByPostIds(@Param("ids") List<Long> ids);

    //Lấy list image theo imageId
    @Query("""
    select i.id,i.imageUrl
    from ImageEntity i
    where i.id in :ids
""")
    List<Object[]> getImageUrlByIds(@Param("ids") List<Long> ids);

    //lấy ra all image theo userId
    @Query("""
    select new com.example.mangxahoi.DTO.Response.ImageResponse(
        i.id,
        i.imageUrl
    )
    from ImageEntity i
    where i.postEntity.userEntity.id = :userId and i.postEntity.groupEntity is null
""")
    List<ImageResponse> getAllImagesByUser(@Param("userId") Long userId);

    //lấy thông tin người đăng bài của bài chia sẻ
    @Query("""
        select new com.example.mangxahoi.DTO.Response.PosterInfoDTO(
            i.id,
            u.id,
            u.fullName,
            u.avatar,
            p.updatedAt
        )
        from ImageEntity i
        join i.postEntity p
        join p.userEntity u
        where i.id in :imageIds
""")
    List<PosterInfoDTO> findPosterInfoByImageIds(
            @Param("imageIds") List<Long> imageIds
    );

    @Query("""
    select i.id, p.id
    from ImageEntity i
    join i.postEntity p
    where i.id in :imageIds
""")
    List<Object[]> findPostIdByImageIds(@Param("imageIds") List<Long> imageIds);
}
