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

    // lấy feed theo userId - trang đầu
    @Query("""
        select f from FeedItemEntity f
        where f.userEntity.id = :userId
        order by f.updatedAt desc, f.id desc
    """)
    List<FeedItemEntity> findFeedSliceFirstPage(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // lấy feed theo userId - trang sau
    @Query("""
        select f from FeedItemEntity f
        where f.userEntity.id = :userId
        and (
            f.updatedAt < :cursorTime
            or (f.updatedAt = :cursorTime and f.id < :cursorId)
        )
        order by f.updatedAt desc, f.id desc
    """)
    List<FeedItemEntity> findFeedSliceNextPage(
            @Param("userId") Long userId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    Optional<FeedItemEntity> findByRefIdAndFeedType(Long refId, FeedType feedType);

    // lấy hết feed - trang đầu
    @Query("""
        select f
        from FeedItemEntity f
        order by f.updatedAt desc, f.id desc
    """)
    List<FeedItemEntity> findAllFeedFirstPage(Pageable pageable);

    // lấy hết feed - trang sau
    @Query("""
        select f
        from FeedItemEntity f
        where f.updatedAt < :cursorTime
           or (f.updatedAt = :cursorTime and f.id < :cursorId)
        order by f.updatedAt desc, f.id desc
    """)
    List<FeedItemEntity> findAllFeedNextPage(
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // lấy feeds theo listIds
    @Query("""
        select f
        from FeedItemEntity f
        where f.feedType = :feedType
        and f.refId in :shareIds
    """)
    List<FeedItemEntity> findAllFeedByRefId(
            @Param("shareIds") List<Long> shareIds,
            @Param("feedType") FeedType feedType
    );

    void deleteByRefIdAndFeedType(Long refId, FeedType feedType);
}













