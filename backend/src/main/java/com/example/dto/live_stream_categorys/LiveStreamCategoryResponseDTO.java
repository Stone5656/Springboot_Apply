/**
 * 自動生成された LiveStreamCategory のレスポンスDTO
 */
package com.example.dto.live_stream_categorys;

import com.example.entity.LiveStreamCategory;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveStreamCategoryResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LiveStreamCategoryResponseDTO fromEntity(LiveStreamCategory entity) {
        return LiveStreamCategoryResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
