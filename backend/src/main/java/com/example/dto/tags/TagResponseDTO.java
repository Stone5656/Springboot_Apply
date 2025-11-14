package com.example.dto.tags;

import com.example.entity.Tag;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * タグ情報レスポンスDTO
 */
@Getter
@Builder
public class TagResponseDTO {

    private UUID id;
    private String name;
    // 必要なら slug も返したい場合はフィールド追加してください
    private String slug;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TagResponseDTO fromEntity(Tag entity) {
        return TagResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
