package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.CommentEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Enums.CommentTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentRepository extends JpaRepository<CommentEntity,Long> {
    Long countByCommentTargetTypeAndTargetId(CommentTargetType commentTargetType, Long targetId);
    Optional<CommentEntity> findById(Long parentId);
    Long countByParentId(Long parentId);

    //lấy ra cmt
    @Query("""
    select c 
    from CommentEntity c 
    join fetch c.userEntity
    where 
    c.targetId = :targetId and c.commentTargetType = :commentTargetType and c.parent is null
    order by c.updatedAt desc
""") Page<CommentEntity> findRootComments(
            @Param("targetId") Long targetId,
            @Param("commentTargetType") CommentTargetType commentTargetType,
            Pageable pageable
    );

    //tính tổng reply của các cmt
    @Query("""
        select c.parent.id,count(c)
        from CommentEntity c
        where c.parent.id in :ids
        group by c.parent.id
""")
    List<Object[]> countReplies(@Param("ids") List<Long> ids);

    //lấy ra list reply
    @Query("""
    select c
    from CommentEntity c
    where c.parent.id = :parentId
    order by c.updatedAt desc
""")
    Page<CommentEntity> getReplies(
            @Param("parentId") Long parentId,
            Pageable pageable
            );

    void deleteByTargetIdAndCommentTargetType(Long targetId, CommentTargetType commentTargetType);

    //lấy ra list id của các reply của comment cha theo id cha
    @Query("""
    select c.id
    from CommentEntity c
    where c.parent.id = :parentId
""")
    List<Long> findReplyIdsByParentId(@Param("parentId") Long parentId);

    //lấy ra list ảnh của các reply của comment cha theo id cha
    @Query("select c.imageUrl from CommentEntity c where c.parent.id = :parentId and c.imageUrl is not null")
    List<String> findReplyImageUrlsByParentId(@Param("parentId") Long parentId);

    @Modifying
    @Query("delete from CommentEntity c where c.parent.id = :parentId")
    int deleteAllByParentId(@Param("parentId") Long parentId);

    // tính tổng từng comment của list targetIds
    @Query("""
        select c.targetId,count(c)
        from CommentEntity c
        where c.commentTargetType = :commentTargetType and c.targetId in :targetIds
        group by c.targetId
""")
    List<Object[]> countCommentByTargetIds(
            @Param("targetIds") List<Long> targetIds,
            @Param("commentTargetType")  CommentTargetType commentTargetType
    );

    //xóa hết các comment theo target
    @Modifying
    @Query("""
    delete
    from CommentEntity c
    where c.targetId in :targetIds
    and c.commentTargetType = :commentTargetType
""")
    void deleteByTargetIdsAndType(@Param("targetIds") List<Long> targetIds, CommentTargetType commentTargetType);

    //lấy ra list commentIds theo target và type
    @Query("""
    select c.id
    from CommentEntity c
    where c.commentTargetType = :commentTargetType and c.targetId in :targetIds
""")
    List<Long> getCommentIdsByTargetAndType(
            @Param("targetIds") List<Long> targetIds,
            CommentTargetType commentTargetType
    );

    void deleteAllByTargetIdAndCommentTargetType(Long targetId, CommentTargetType commentTargetType);
}
