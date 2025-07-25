/**
 * 自動生成された LiveStreamTag のレスポンスDTO
 */
package com.example.dto.live_stream_tags;

import com.example.entity.LiveStreamTag;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveStreamTagResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LiveStreamTagResponseDTO fromEntity(LiveStreamTag entity) {
        return LiveStreamTagResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
