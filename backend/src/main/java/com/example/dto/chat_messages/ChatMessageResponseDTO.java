/**
 * 自動生成された ChatMessage のレスポンスDTO
 */
package com.example.dto.chat_messages;

import com.example.entity.ChatMessage;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatMessageResponseDTO fromEntity(ChatMessage entity) {
        return ChatMessageResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
