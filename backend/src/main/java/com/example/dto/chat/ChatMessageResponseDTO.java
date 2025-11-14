package com.example.dto.chat;

import com.example.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * チャットメッセージのレスポンスDTO
 */
@Getter
@Builder
public class ChatMessageResponseDTO {

    private UUID id;

    /** 投稿先ライブ配信ID */
    private UUID liveStreamId;

    /** 投稿者ユーザーID */
    private UUID userId;

    /** メッセージ本文 */
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatMessageResponseDTO fromEntity(ChatMessage entity) {
        return ChatMessageResponseDTO.builder()
                .id(entity.getId())
                .liveStreamId(entity.getLiveStream().getId())
                .userId(entity.getUser().getId())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
