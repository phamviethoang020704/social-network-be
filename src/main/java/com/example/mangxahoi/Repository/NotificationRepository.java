package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.NotificationEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByReceiverOrderByCreatedAtDesc(UserEntity receiver);

    long countByReceiverAndReadFalse(UserEntity receiver);
    Optional<NotificationEntity> findByIdAndReceiver(Long id, UserEntity receiver);

    void deleteByReceiverAndActorAndNotificationType(
            UserEntity receiver,
            UserEntity actor,
            NotificationType notificationType
    );
}