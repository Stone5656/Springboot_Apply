package com.example.controller;

import com.example.dto.subscriptions.*;
import com.example.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Subscription API
 *
 * ユーザー間フォロー（subscriber / target）を扱う。
 * 公開 → 認証必須 の順にセクション化。
 */
@Tag(name = "Subscriptions", description = "フォロー関連API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // ========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // ========================================================

    @Operation(
            summary = "フォロー一覧取得",
            description = "指定ユーザーがフォローしているユーザー一覧を取得します（公開）"
    )
    @GetMapping("/users/{userId}/subscriptions")
    public ResponseEntity<List<SubscriptionResponseDTO>> getSubscriptionsBySubscriber(
            @PathVariable UUID userId
    ) {
        List<SubscriptionResponseDTO> list =
                subscriptionService.getSubscriptionsBySubscriber(userId);
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "フォロワー一覧取得",
            description = "指定ユーザーをフォローしているユーザー一覧を取得します（公開）"
    )
    @GetMapping("/users/{userId}/subscribers")
    public ResponseEntity<List<SubscriptionResponseDTO>> getSubscriptionsByTarget(
            @PathVariable UUID userId
    ) {
        List<SubscriptionResponseDTO> list =
                subscriptionService.getSubscriptionsByTarget(userId);
        return ResponseEntity.ok(list);
    }

    // ========================================================
    // ============ Ⅱ. 認証必須（Authenticated） ==============
    // ========================================================

    @Operation(
            summary = "フォロー登録",
            description = "ユーザーをフォローします（認証必須）"
    )
    @PostMapping("/subscriptions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(
            @Valid @RequestBody SubscriptionCreateRequestDTO request
    ) {
        // 実運用では SecurityContext のユーザーIDと request.subscriberId の整合性チェック推奨
        SubscriptionResponseDTO created = subscriptionService.createSubscription(request);
        return ResponseEntity.ok(created);
    }

    @Operation(
            summary = "フォロー解除",
            description = "ユーザーのフォローを解除します（認証必須）"
    )
    @DeleteMapping("/subscriptions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeSubscription(
            @Valid @RequestBody SubscriptionRemoveRequestDTO request
    ) {
        subscriptionService.removeSubscription(request);
        return ResponseEntity.noContent().build();
    }
}
