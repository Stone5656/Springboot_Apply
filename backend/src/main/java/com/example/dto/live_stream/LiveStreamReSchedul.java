package com.example.dto.live_stream;

import com.example.enums.StreamStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveStreamReSchedul {
    @NotBlank
    private LocalDateTime scheduledAt;
    private StreamStatus status;
}
