package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.Response.GroupRes.ListGroupRes;
import com.example.mangxahoi.Entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity,Long> {
    @Query("""
        select new com.example.mangxahoi.DTO.Response.GroupRes.ListGroupRes(
            g.groupEntity.userEntity.avatar,
            
            g.groupEntity.groupName,
            g.groupEntity.isPublic,
            g.groupEntity.id,
            g.groupEntity.coverPhoto
        )
        from GroupMemberEntity g
        where g.userEntity.id = :userId
""")
    List<ListGroupRes> getListGroupByUserId(@Param("userId")  Long userId);
}
