package com.example.mangxahoi.Mapper;

import com.example.mangxahoi.DTO.Response.TagResponse;
import com.example.mangxahoi.Entity.TagEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.TagRepository;
import com.example.mangxahoi.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TagMapper {
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public List<TagResponse> toResponse(List<TagEntity> tagEntities) {
        return tagEntities.stream()
                .map(t ->{
                    TagResponse tagResponse = new TagResponse();
                    tagResponse.setId(t.getUserEntity().getId());
                    tagResponse.setNameTagged(
                            t.getUserEntity().getFullName()
                    );
                    return tagResponse;
                }).toList();
    }
}
