package com.example.dto.live_stream;

import com.example.entity.LiveStream;
import com.example.enums.StreamStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class LiveStreamResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private String thumbnailPath;
    private StreamStatus status;
    private Long viewsCount;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LiveStreamResponseDTO fromEntity(LiveStream liveStream) {
        return LiveStreamResponseDTO.builder()
                .id(liveStream.getId())
                .title(liveStream.getTitle())
                .description(liveStream.getDescription())
                .thumbnailPath(liveStream.getThumbnailPath())
                .status(liveStream.getStatus())
                .viewsCount(liveStream.getViewsCount())
                .scheduledAt(liveStream.getScheduledAt())
                .startedAt(liveStream.getStartedAt())
                .endedAt(liveStream.getEndedAt())
                .createdAt(liveStream.getCreatedAt())
                .updatedAt(liveStream.getUpdatedAt())
                .build();
    }
}
