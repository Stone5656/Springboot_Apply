package com.example.dto.live_stream;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveStreamCreateRequestDTO {
    @NotBlank
    private String title;

    private String description;
    private String thumbnailPath;
    private LocalDateTime scheduledAt;
}
