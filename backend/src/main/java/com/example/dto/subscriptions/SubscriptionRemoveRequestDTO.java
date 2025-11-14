package com.example.dto.subscriptions;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Subscription解除用（アンフォロー）リクエストDTO
 *
 * - 「誰が誰を外すか」の情報だけを持つシンプルなDTO。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRemoveRequestDTO {

    /** フォローする側（通常はログインユーザー） */
    private UUID subscriberId;

    /** フォローをやめる対象（必須） */
    @NotNull
    private UUID targetId;
}
