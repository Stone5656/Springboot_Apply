package com.example.dto.videos;

import com.example.enums.VideoVisibility;
import com.example.entity.Video;
import com.example.enums.VideoStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VideoResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String thumbnailPath;
    private Integer duration; // 秒数
    private VideoVisibility visibility;
    private VideoStatus status;
    private Long viewsCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VideoResponseDTO fromEntity(Video video) {
        return VideoResponseDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .thumbnailPath(video.getThumbnailPath())
                .duration(video.getDuration())
                .visibility(video.getVisibility())
                .status(video.getStatus())
                .viewsCount(video.getViewsCount())
                .publishedAt(video.getPublishedAt())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }
}

