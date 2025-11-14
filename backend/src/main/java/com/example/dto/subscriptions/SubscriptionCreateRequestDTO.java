package com.example.dto.subscriptions;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Subscription作成用リクエストDTO
 *
 * - 通常は subscriberId は「認証ユーザーのID」からService側で補完するのが安全。
 * - ここでは汎用性を重視して両方持たせています。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionCreateRequestDTO {

    /** フォローする側のユーザーID（nullならログインユーザーから補完する想定も可能） */
    private UUID subscriberId;

    /** フォローされる側（必須） */
    @NotNull
    private UUID targetId;
}
