package com.example.dto.tags;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;
import java.util.UUID;

/**
 * 動画にタグを追加するためのリクエストDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoTagsAddRequest {

    @NotNull
    private UUID videoId;

    @NotEmpty
    @UniqueElements
    private List<@NotNull UUID> tagIds;
}
