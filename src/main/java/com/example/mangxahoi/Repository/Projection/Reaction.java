package com.example.mangxahoi.Repository.Projection;

import com.example.mangxahoi.Enums.ReactionType;

public interface Reaction {
    Long getTargetId();
    ReactionType getReactionType();
}
