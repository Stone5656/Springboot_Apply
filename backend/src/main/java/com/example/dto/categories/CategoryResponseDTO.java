package com.example.dto.categories;

import com.example.entity.Category;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * CategoryのレスポンスDTO
 */
@Getter
@Builder
public class CategoryResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryResponseDTO fromEntity(Category entity) {
        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
