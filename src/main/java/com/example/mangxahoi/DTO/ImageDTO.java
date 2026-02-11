package com.example.mangxahoi.DTO;

import com.example.mangxahoi.Entity.ImageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImageDTO {
    private ImageEntity current;
    private ImageEntity prev;
    private ImageEntity next;
}
