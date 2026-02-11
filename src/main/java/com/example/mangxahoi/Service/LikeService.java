package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.ReactionCountDTO;
import com.example.mangxahoi.DTO.Response.LikeResponse;
import com.example.mangxahoi.Entity.LikeEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.LikeTargetType;
import com.example.mangxahoi.Enums.ReactionType;
import com.example.mangxahoi.Repository.LikeRepository;
import com.example.mangxahoi.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LikeService {
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    public LikeService(UserRepository userRepository, LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public LikeResponse ToggleLike(String username, Long targetId, LikeTargetType targetType, ReactionType reactionType) {
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
        Optional<LikeEntity> existing = likeRepository.findByUserEntityAndLikeTargetTypeAndTargetId(userEntity, targetType, targetId);
        boolean liked;
        ReactionType currentReaction = null;
        if (existing.isPresent()) {
            LikeEntity likeEntity = existing.get();

            if (likeEntity.getReactionType() == reactionType) {
                likeRepository.delete(likeEntity);
                liked = false;
            } else {
                likeEntity.setReactionType(reactionType);
                likeRepository.save(likeEntity);
                liked = true;
                currentReaction = reactionType;
            }
        }
        else {
            LikeEntity like = new LikeEntity();
            like.setUserEntity(userEntity);
            like.setTargetId(targetId);
            like.setLikeTargetType(targetType);
            like.setReactionType(reactionType);
            likeRepository.save(like);

            liked = true;
            currentReaction = reactionType;
        }
        long likeCount = likeRepository.countByLikeTargetTypeAndTargetId(
                targetType, targetId
        );

        return new LikeResponse(liked, currentReaction, likeCount);
    }

    public List<ReactionCountDTO> getFullReaction(Long targetId, LikeTargetType targetType) {

        // DB trả về reaction có tồn tại
        List<ReactionCountDTO> dbResult =
                likeRepository.countReaction(targetId,targetType);

        // Map để dễ lookup
        Map<ReactionType, Long> reactionMap = new EnumMap<>(ReactionType.class);

        // set mặc định = 0
        for (ReactionType type : ReactionType.values()) {
            reactionMap.put(type, 0L);
        }

        // ghi đè dữ liệu từ DB
        for (ReactionCountDTO dto : dbResult) {
            reactionMap.put(dto.getReactionType(), dto.getTotal());
        }

        // convert lại List
        return reactionMap.entrySet().stream()
                .map(e -> new ReactionCountDTO(e.getKey(), e.getValue()))
                .toList();
    }
}