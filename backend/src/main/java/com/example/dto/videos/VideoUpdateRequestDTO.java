package com.example.dto.videos;

import com.example.enums.VideoStatus;
import com.example.enums.VideoVisibility;
import lombok.Getter;

@Getter
public class VideoUpdateRequestDTO {
    private String title;
    private String description;
    private String thumbnailPath;
    private VideoVisibility visibility;
    private VideoStatus status;
}
