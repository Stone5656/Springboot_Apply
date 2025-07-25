/**
 * 自動生成された Tag のレスポンスDTO
 */
package com.example.dto.tags;

import com.example.entity.Tag;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TagResponseDTO fromEntity(Tag entity) {
        return TagResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
