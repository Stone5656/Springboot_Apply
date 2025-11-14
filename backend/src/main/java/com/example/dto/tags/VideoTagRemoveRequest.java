package com.example.dto.tags;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * 動画から特定のタグを1つ外すためのリクエストDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoTagRemoveRequest {

    @NotNull
    private UUID videoId;

    @NotNull
    private UUID tagId;
}
