package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ライブ配信中のチャットメッセージを表すエンティティ。
 * 各メッセージには配信とユーザーの関連が必要。
 *
 * @version 2.0
 */
@Entity
@Table(
    name = "chat_messages",
    indexes = {
        @Index(name = "idx_chat_message_live_stream_id", columnList = "live_stream_id"),
        @Index(name = "idx_chat_message_user_id", columnList = "user_id")
    }
)
@Getter
public class ChatMessage {

    /** 主キーUUID（BINARY(16)） */
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    /** メッセージを投稿した配信ID */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_stream_id", nullable = false)
    private LiveStream liveStream;

    /** メッセージ投稿者のユーザーID */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** メッセージ本文 */
    @NotBlank
    @Size(max = 500)
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** 作成日時 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新日時 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ====================================================
    // ================= コンストラクタ ===================
    // ====================================================

    /**
     * ChatMessage エンティティの唯一のコンストラクタ
     *
     * @param liveStream ライブ配信
     * @param user ユーザー
     * @param message メッセージ本文
     */
    public ChatMessage(LiveStream liveStream, User user, String message) {
        this.liveStream = liveStream;
        this.user = user;
        this.message = message;
    }

    // ====================================================
    // =================== 業務ロジック ===================
    // ====================================================

    /**
     * メッセージの文字数が制限内かを検証します。
     *
     * @return true = 500文字以内
     */
    public boolean isValidLength() {
        return this.message != null && this.message.length() <= 500;
    }

    /**
     * メッセージの投稿者が指定されたユーザーかを検証します。
     *
     * @param user 対象ユーザー
     * @return 一致すればtrue
     */
    public boolean isPostedBy(User user) {
        return this.user != null && this.user.equals(user);
    }

    /**
     * 指定された配信への投稿かを検証します。
     *
     * @param stream 対象配信
     * @return 一致すればtrue
     */
    public boolean isForStream(LiveStream stream) {
        return this.liveStream != null && this.liveStream.equals(stream);
    }
}
