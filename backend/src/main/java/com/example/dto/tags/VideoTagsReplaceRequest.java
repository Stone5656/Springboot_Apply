package com.example.dto.tags;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;
import java.util.UUID;

/**
 * 動画に紐づくタグ一覧を「置き換える」ためのリクエストDTO
 * 空配列を「タグ0件＝全削除」として扱うかどうかは Service 側のポリシーに依存。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoTagsReplaceRequest {

    @NotNull
    private UUID videoId;

    @NotNull         // null は不可（空リストはOKにしてもよい）
    @UniqueElements
    private List<@NotNull UUID> tagIds;
}
