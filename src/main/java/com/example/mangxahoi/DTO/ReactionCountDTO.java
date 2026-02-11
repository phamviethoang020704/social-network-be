package com.example.mangxahoi.DTO;

import com.example.mangxahoi.Enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReactionCountDTO {
    private ReactionType reactionType;
    private Long total;
}
