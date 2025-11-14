package com.example.dto.tags;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;
import java.util.UUID;

/**
 * ライブ配信にタグを追加するためのリクエストDTO
 * （既存タグに「追加」する用途）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStreamTagsAddRequest {

    @NotNull
    private UUID liveStreamId;

    @NotEmpty
    @UniqueElements   // 重複IDを防ぐ
    private List<@NotNull UUID> tagIds;
}
