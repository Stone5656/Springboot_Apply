package com.example.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * チャットメッセージ投稿用リクエストDTO
 *
 * - 本来 userId は認証情報から取るのが安全だが、
 *   必要に応じてバッチ等で使えるよう optional にしている。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageCreateRequestDTO {

    /** 投稿先ライブ配信ID（必須） */
    @NotNull
    private UUID liveStreamId;

    /** 投稿者ユーザーID（通常はSecurityContextから補完） */
    private UUID userId;

    /** メッセージ本文（500文字まで） */
    @NotBlank
    @Size(max = 500)
    private String message;
}
