//package com.example.mangxahoi.Repository;
//
//import com.example.mangxahoi.Entity.UserEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface UserRepository extends JpaRepository<UserEntity,Integer> {
//    Optional<UserEntity> findByUsername(String username);
//    boolean existsByUsername(String username);
//}
package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    boolean existsByUsername(String username);
    Optional<UserEntity> findByUsername(String username);

}