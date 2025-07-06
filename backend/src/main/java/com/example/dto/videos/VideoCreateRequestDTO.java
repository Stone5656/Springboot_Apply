package com.example.dto.videos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VideoCreateRequestDTO {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String videoPath;

    private String thumbnailPath;

}

