package com.example.controller;

import com.example.dto.chat.*;
import com.example.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ChatMessage API
 *
 * ライブ配信中のチャットメッセージを扱う。
 * 公開 → 認証必須 の順にセクション化。
 */
@Tag(name = "ChatMessages", description = "チャット関連API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // ========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // ========================================================

    @Operation(
            summary = "チャットメッセージ取得",
            description = "IDでチャットメッセージを1件取得します（公開）"
    )
    @GetMapping("/chat/messages/{id}")
    public ResponseEntity<ChatMessageResponseDTO> getMessage(@PathVariable UUID id) {
        ChatMessageResponseDTO dto = chatMessageService.getMessage(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "チャットメッセージ検索",
            description = "配信IDやユーザーID、キーワードでチャットメッセージを検索します（公開）"
    )
    @GetMapping("/chat/messages")
    public ResponseEntity<Page<ChatMessageResponseDTO>> searchMessages(
            @ParameterObject @Valid ChatMessageSearchRequestDTO request,
            @ParameterObject Pageable pageable
    ) {
        Page<ChatMessageResponseDTO> page = chatMessageService.searchMessages(request, pageable);
        return ResponseEntity.ok(page);
    }

    // ========================================================
    // ============ Ⅱ. 認証必須（Authenticated） ==============
    // ========================================================

    @Operation(
            summary = "チャットメッセージ投稿",
            description = "ライブ配信にチャットメッセージを投稿します（認証必須）"
    )
    @PostMapping("/chat/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageResponseDTO> createMessage(
            @Valid @RequestBody ChatMessageCreateRequestDTO request
    ) {
        // 実運用では SecurityContext のユーザーIDで request.userId を上書きする運用が安全
        ChatMessageResponseDTO created = chatMessageService.createMessage(request);
        return ResponseEntity.created(URI.create("/api/chat/messages/" + created.getId()))
                .body(created);
    }
}
