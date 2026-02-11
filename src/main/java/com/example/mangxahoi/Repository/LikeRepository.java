package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.ReactionCountDTO;
import com.example.mangxahoi.Entity.LikeEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.LikeTargetType;
import com.example.mangxahoi.Enums.ReactionType;
import com.example.mangxahoi.Repository.Projection.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    Optional<LikeEntity> findByUserEntityAndLikeTargetTypeAndTargetId(UserEntity userEntity, LikeTargetType likeTargetType, Long targetId);

    //kiểm tra xem người dùng đã like vào target chưa
    boolean existsByUserEntityAndTargetIdAndLikeTargetType(UserEntity userEntity, Long targetId, LikeTargetType likeTargetType);

    Long countByLikeTargetTypeAndTargetId(LikeTargetType likeTargetType, Long targetId);

    Optional<LikeEntity> findByUserEntityAndTargetId(UserEntity userEntity, Long targetId);

    //tổng từng reaction
    @Query("""
        SELECT new com.example.mangxahoi.DTO.ReactionCountDTO(
            l.reactionType,
            COUNT(l.id)
        )
        FROM LikeEntity l
        WHERE l.targetId = :targetId
          AND l.likeTargetType = :targetType
        GROUP BY l.reactionType
    """)
    List<ReactionCountDTO> countReaction(
            @Param("targetId") Long targetId,
            @Param("targetType") LikeTargetType targetType
    );

    //tính tổng like của từng target
    @Query("""
    select l.targetId,count(l)
    from LikeEntity l
    where l.targetId in :ids and l.likeTargetType = :likeTargetType
    group by l.targetId
""")
    List<Object[]> countLikes(@Param("likeTargetType")  LikeTargetType likeTargetType, @Param("ids") List<Long> ids);

    //kiểm tra xem đã like chưa của danh sách target
    @Query("""
        select l.targetId,l.reactionType
        from LikeEntity l
        where l.targetId in :ids and l.userEntity.id = :userId and l.likeTargetType = :likeTargetType
""")
    List<Object[]> existsReaction(
            @Param("userId") Long userId,
            @Param("likeTargetType") LikeTargetType likeTargetType,
            @Param("ids") List<Long> ids);

    //Lấy ra list Like của post
    @Query("""
        select l
        from LikeEntity l
        where l.likeTargetType = :likeTargetType
        and l.targetId in :p
""")
    List<LikeEntity> findUserLiked(
            @Param("likeTargetType") LikeTargetType likeTargetType,
            @Param("user_id") Long userId,
            @Param("postIds") List<Long> postIds
    );

    void deleteByTargetIdAndLikeTargetType(Long targetId, LikeTargetType likeTargetType);

    @Modifying
    @Query("""
    delete from LikeEntity l
    where l.likeTargetType = :likeTargetType
    and l.targetId in :ids
""")
    void deleteByTargetIdsAndLikeTargetType(@Param("ids") List<Long> ids,@Param("likeTargetType") LikeTargetType likeTargetType);

    // lấy ra isLiked và reactionType
    @Query("""
    select l.targetId as targetId,
           l.reactionType as reactionType
    from LikeEntity l
    where l.userEntity = :user
      and l.likeTargetType = :likeTargetType
      and l.targetId in :targetIds
""")
    List<Reaction> findUserReactions(
            @Param("user") UserEntity user,
            @Param("likeTargetType") LikeTargetType likeTargetType,
            @Param("targetIds")  List<Long> targetIds
    );

    void deleteAllByTargetIdAndLikeTargetType(Long targetId, LikeTargetType likeTargetType);

    ReactionType findReactionTypeByUserEntityAndLikeTargetTypeAndTargetId(UserEntity userEntity, LikeTargetType likeTargetType, Long targetId);

    @Query("""
    select l.reactionType
    from LikeEntity l
    where l.userEntity.id = :userId
      and l.likeTargetType = :type
      and l.targetId = :targetId
""")
    Optional<ReactionType> findReactionByUserAndTargetAndTargetId(
            @Param("userId") Long userId,
            @Param("targetId") Long targetId,
            @Param("type") LikeTargetType type
    );
}

