/**
 * 自動生成された Category のレスポンスDTO
 */
package com.example.dto.categorys;

import com.example.entity.Category;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryResponseDTO fromEntity(Category entity) {
        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
