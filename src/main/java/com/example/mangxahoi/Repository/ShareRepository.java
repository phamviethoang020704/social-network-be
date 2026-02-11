package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.Response.ShareResponse;
import com.example.mangxahoi.Entity.ShareEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.ShareType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShareRepository extends JpaRepository<ShareEntity,Long> {
    Long countByShareTypeAndTargetId(ShareType shareType, Long targetId);

    boolean existsByUserEntityAndTargetIdAndShareType(UserEntity userEntity, Long targetId, ShareType shareType);

    List<ShareEntity> findByUserEntityId(Long userId);

    Optional<List<ShareEntity>> findAllByTargetIdAndShareType(Long targetId, ShareType shareType);

    @Query("""
        select s.targetId, count(s)
        from ShareEntity s
        where s.shareType = :type
          and s.targetId in :targetIds
        group by s.targetId
    """)
    List<Object[]> countSharesByTargetIds(@Param("type") ShareType type,
                                          @Param("targetIds") List<Long> targetIds);
}
