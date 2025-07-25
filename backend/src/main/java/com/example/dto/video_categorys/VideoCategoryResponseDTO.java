/**
 * 自動生成された VideoCategory のレスポンスDTO
 */
package com.example.dto.video_categorys;

import com.example.entity.VideoCategory;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoCategoryResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VideoCategoryResponseDTO fromEntity(VideoCategory entity) {
        return VideoCategoryResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
