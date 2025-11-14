package com.example.dto.subscriptions;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.entity.Subscription;

/**
 * Subscription（ユーザー間フォロー）のレスポンスDTO
 */
@Getter
@Builder
public class SubscriptionResponseDTO {

    private UUID id;

    /** フォローする側（自分） */
    private UUID subscriberId;

    /** フォローされる側（相手） */
    private UUID targetId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubscriptionResponseDTO fromEntity(Subscription entity) {
        return SubscriptionResponseDTO.builder()
                .id(entity.getId())
                .subscriberId(entity.getSubscriber().getId())
                .targetId(entity.getTarget().getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
