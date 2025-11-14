package com.example.repository;

import com.example.entity.ChatMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ライブ配信中のチャットメッセージ用リポジトリ
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /** 配信単位のメッセージ一覧（ページング） */
    Page<ChatMessage> findByLiveStream_IdOrderByCreatedAtAsc(UUID liveStreamId, Pageable pageable);

    /** 配信 + ユーザー単位でのメッセージ一覧（ページング） */
    Page<ChatMessage> findByLiveStream_IdAndUser_IdOrderByCreatedAtAsc(
            UUID liveStreamId,
            UUID userId,
            Pageable pageable
    );

    /** 配信 + キーワード（本文部分一致） */
    Page<ChatMessage> findByLiveStream_IdAndMessageContainingIgnoreCaseOrderByCreatedAtAsc(
            UUID liveStreamId,
            String keyword,
            Pageable pageable
    );

    /** 配信 + ユーザー + キーワード */
    Page<ChatMessage> findByLiveStream_IdAndUser_IdAndMessageContainingIgnoreCaseOrderByCreatedAtAsc(
            UUID liveStreamId,
            UUID userId,
            String keyword,
            Pageable pageable
    );
}
