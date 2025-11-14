package com.example.dto.tags;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * ライブ配信から特定のタグを1つ外すためのリクエストDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStreamTagRemoveRequest {

    @NotNull
    private UUID liveStreamId;

    @NotNull
    private UUID tagId;
}
