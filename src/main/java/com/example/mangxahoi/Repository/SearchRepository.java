package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.RecentSearchEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SearchRepository extends JpaRepository<RecentSearchEntity, Long> {
    List<RecentSearchEntity> findTop8ByUserEntity_IdOrderByLastUsedAtDesc(Long userId);

    @Query("""
      select r from RecentSearchEntity r
      where r.userEntity.id = :userId
        and lower(r.keyword) like lower(concat('%', :q, '%'))
      order by
        case
          when lower(r.keyword) = lower(:q) then 0
          when lower(r.keyword) like lower(concat(:q, '%')) then 1
          else 2
        end,
        r.lastUsedAt desc
    """)
    List<RecentSearchEntity> suggestRecent(@Param("userId") Long userId,
                                           @Param("q") String q,
                                           Pageable pageable);

    Optional<RecentSearchEntity> findByUserEntity_IdAndKeyword(Long userId, String keyword);

    long countByUserEntity_Id(Long userId);

    Optional<RecentSearchEntity> findFirstByUserEntity_IdOrderByLastUsedAtAsc(Long userId);

    void deleteByIdAndUserEntity_Id(Long id, Long userId);
}
