/**
 * 自動生成された Video のレスポンスDTO
 */
package com.example.dto.videos;

import com.example.entity.Video;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VideoResponseDTO fromEntity(Video entity) {
        return VideoResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
