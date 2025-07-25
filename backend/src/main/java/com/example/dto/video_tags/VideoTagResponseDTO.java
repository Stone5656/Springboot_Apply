/**
 * 自動生成された VideoTag のレスポンスDTO
 */
package com.example.dto.video_tags;

import com.example.entity.VideoTag;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoTagResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VideoTagResponseDTO fromEntity(VideoTag entity) {
        return VideoTagResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
