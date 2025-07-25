/**
 * 自動生成された LiveStream のレスポンスDTO
 */
package com.example.dto.live_streams;

import com.example.entity.LiveStream;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveStreamResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LiveStreamResponseDTO fromEntity(LiveStream entity) {
        return LiveStreamResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
