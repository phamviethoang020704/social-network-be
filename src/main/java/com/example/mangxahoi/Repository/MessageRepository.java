package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Objects;

public interface MessageRepository extends JpaRepository<MessageEntity,Long> {
    List<MessageEntity> findByConversationIdOrderByIdDesc(Long conversationId, Pageable pageable);

    List<MessageEntity> findByConversationIdAndIdLessThanOrderByIdDesc(
            Long conversationId,
            Long beforeId,
            Pageable pageable
    );

}
