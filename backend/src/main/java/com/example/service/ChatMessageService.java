package com.example.service;

import com.example.dto.chat.*;
import com.example.entity.ChatMessage;
import com.example.entity.LiveStream;
import com.example.entity.User;
import com.example.repository.ChatMessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ライブ配信のチャットメッセージ用サービス.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @PersistenceContext
    private EntityManager em;

    // ========================================================
    // =============== Ⅰ. 参照系（Public想定） ================
    // ========================================================

    /**
     * ChatMessageSearchRequestDTO の条件に応じてメッセージを検索.
     *
     * - liveStreamId は必須
     * - userId / keyword は任意
     */
    public Page<ChatMessageResponseDTO> searchMessages(ChatMessageSearchRequestDTO req,
                                                       Pageable pageable) {
        if (req == null || req.getLiveStreamId() == null) {
            throw new IllegalArgumentException("liveStreamId は必須です。");
        }

        UUID liveStreamId = req.getLiveStreamId();
        UUID userId = req.getUserId();
        String keyword = req.getKeyword();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        Page<ChatMessage> page;

        if (userId != null && hasKeyword) {
            page = chatMessageRepository
                    .findByLiveStream_IdAndUser_IdAndMessageContainingIgnoreCaseOrderByCreatedAtAsc(
                            liveStreamId, userId, keyword, pageable);
        } else if (userId != null) {
            page = chatMessageRepository
                    .findByLiveStream_IdAndUser_IdOrderByCreatedAtAsc(liveStreamId, userId, pageable);
        } else if (hasKeyword) {
            page = chatMessageRepository
                    .findByLiveStream_IdAndMessageContainingIgnoreCaseOrderByCreatedAtAsc(
                            liveStreamId, keyword, pageable);
        } else {
            page = chatMessageRepository
                    .findByLiveStream_IdOrderByCreatedAtAsc(liveStreamId, pageable);
        }

        return page.map(ChatMessageResponseDTO::fromEntity);
    }

    // ========================================================
    // ============ Ⅱ. 更新系（Authenticated想定） ============
    // ========================================================

    /**
     * チャットメッセージの投稿.
     *
     * userId も DTO にありますが、実運用上は認証ユーザーIDを使う方が安全です。
     */
    @Transactional
    public ChatMessageResponseDTO createMessage(ChatMessageCreateRequestDTO req) {
        if (req.getLiveStreamId() == null) {
            throw new IllegalArgumentException("liveStreamId は必須です。");
        }
        if (req.getUserId() == null) {
            throw new IllegalArgumentException("userId は必須です（認証情報から補完する運用を推奨）。");
        }
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            throw new IllegalArgumentException("メッセージ本文は必須です。");
        }

        // 参照プロキシで関連エンティティを取得（SELECTを避ける）
        LiveStream liveRef = em.getReference(LiveStream.class, req.getLiveStreamId());
        User userRef = em.getReference(User.class, req.getUserId());

        ChatMessage entity = new ChatMessage(liveRef, userRef, req.getMessage());

        // エンティティ側にも長さチェックロジックあり（isValidLength）。
        if (!entity.isValidLength()) {
            throw new IllegalArgumentException("メッセージは500文字以内である必要があります。");
        }

        ChatMessage saved = chatMessageRepository.save(entity);
        return ChatMessageResponseDTO.fromEntity(saved);
    }

    /**
     * （必要であれば）単一メッセージ取得用.
     */
    public ChatMessageResponseDTO getMessage(UUID id) {
        ChatMessage msg = chatMessageRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("メッセージが見つかりません (ID: " + id + ")"));
        return ChatMessageResponseDTO.fromEntity(msg);
    }
}
