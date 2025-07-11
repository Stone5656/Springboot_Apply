package com.example.dto.live_stream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveStreamUpdateRequestDTO {
    private String title;
    private String description;
    private String thumbnailPath;
}
