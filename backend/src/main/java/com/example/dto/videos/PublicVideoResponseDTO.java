package com.example.dto.videos;

import com.example.entity.Video;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class PublicVideoResponseDTO {
    private UUID id;
    private String title;
    private String thumbnailPath;
    private Long viewsCount;
    private UploaderDTO uploader;

    public static PublicVideoResponseDTO fromEntity(Video video)
    {
        return PublicVideoResponseDTO.builder().id(video.getId()).title(video.getTitle())
                .thumbnailPath(video.getThumbnailPath()).viewsCount(video.getViewsCount())
                .uploader(UploaderDTO.fromUser(video.getUser())).build();
    }
}
