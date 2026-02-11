package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<TagEntity, Long> {
    void deleteByPostEntity(PostEntity postEntity);
    List<TagEntity> findByPostEntity(PostEntity postEntity);

    void deleteByPostEntityId(Long postEntityId);
}
