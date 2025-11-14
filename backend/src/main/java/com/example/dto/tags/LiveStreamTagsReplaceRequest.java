package com.example.dto.tags;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;
import java.util.UUID;

/**
 * ライブ配信に紐づくタグ一覧を「置き換える」ためのリクエストDTO
 * 空配列を許可する場合は Service 側で「全削除」と解釈させる想定。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStreamTagsReplaceRequest {

    @NotNull
    private UUID liveStreamId;

    @NotNull               // null は不可（空配列は「0件」を意味させる）
    @UniqueElements
    private List<@NotNull UUID> tagIds;
}
