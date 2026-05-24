package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity,Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUsername(String username);
}
