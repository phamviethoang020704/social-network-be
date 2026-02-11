package com.example.mangxahoi.Repository;

import com.example.mangxahoi.DTO.Chat.ConversationResponse;
import com.example.mangxahoi.Entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<ConversationEntity,Long> {
    Optional<ConversationEntity> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    @Query("""
    select c 
    from ConversationEntity c
    where (c.user1.id = :user1Id and c.user2.id = :user2Id)
    or (c.user1.id = :user2Id and c.user2.id = :user1Id)
""")
    Optional<ConversationEntity> lookUpConversation(Long user1Id, Long user2Id);


    //lấy ra tất cả cuộc trò chuyện
    @Query("""
    select c
    from ConversationEntity c
        join fetch c.user1
        join fetch c.user2
    where c.user1.id = :userId or c.user2.id = :userId
    order by c.lastMessageAt desc
""")
    List<ConversationEntity> getAllConversations(@Param("userId") Long userId);
}
