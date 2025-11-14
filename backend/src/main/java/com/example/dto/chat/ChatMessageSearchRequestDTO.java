package com.example.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * チャットメッセージ検索用リクエストDTO
 *
 * - 主に「配信単位のメッセージ一覧」を取る想定。
 *   必要に応じて userId/keyword で絞り込み。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageSearchRequestDTO {

    /** 対象ライブ配信（必須） */
    @NotNull
    private UUID liveStreamId;

    /** 特定ユーザーのメッセージだけに絞りたい場合に使用（任意） */
    private UUID userId;

    /** メッセージ本文の部分一致検索用キーワード（任意） */
    @Size(max = 100)
    private String keyword;
}
