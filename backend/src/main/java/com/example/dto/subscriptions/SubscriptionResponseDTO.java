/**
 * 自動生成された Subscription のレスポンスDTO
 */
package com.example.dto.subscriptions;

import com.example.entity.Subscription;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubscriptionResponseDTO fromEntity(Subscription entity) {
        return SubscriptionResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
