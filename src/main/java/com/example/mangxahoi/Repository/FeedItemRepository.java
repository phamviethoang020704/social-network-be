package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.FeedItemEntity;
import com.example.mangxahoi.Enums.FeedType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FeedItemRepository extends JpaRepository<FeedItemEntity, Long> {

    //lấy feed theo userId
    @Query("""
   select f from FeedItemEntity f
   where f.userEntity.id = :userId
   and (
    :cursorTime is null
    or f.updatedAt < :cursorTime
    or (f.updatedAt = :cursorTime and f.id < :cursorId)
   )
   order by f.updatedAt desc, f.id desc
""")
    List<FeedItemEntity> findFeedSlice(
            @Param("userId") Long userId,
            @Param("cursorTime")LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
            );

    Optional<FeedItemEntity> findByRefIdAndFeedType(Long refId, FeedType feedType);

    //lấy hết feed
    @Query("""
   select f 
   from FeedItemEntity f
   where :cursorTime is null 
   or f.updatedAt < :cursorTime 
   or (f.updatedAt = :cursorTime and f.id < :cursorId)
   order by f.updatedAt desc, f.id desc
""")
    List<FeedItemEntity> findAllFeed(
            @Param("cursorTime")LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    //Lấy feeds theo listIds
    @Query("""
        select f
        from FeedItemEntity f
        where f.feedType = :feedType
        and
        f.refId in :shareIds
""")
    List<FeedItemEntity> findAllFeedByRefId(
            @Param("shareIds") List<Long> shareIds,
            FeedType feedType
    );

    void deleteByRefIdAndFeedType(Long refId, FeedType feedType);
}













