package com.example.entity;

import com.example.enums.StreamStatus;
import com.example.util.entity.AbstractSoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.springframework.util.Assert;

/**
 * ライブ配信に関する情報を管理するエンティティ。 配信のタイトル、説明、配信ステータスやスケジュール、削除状態などを保持する。
 *
 * @version 1.2
 */
@Entity
@Table(name = "live_streams",
        indexes = {@Index(name = "idx_live_stream_user_id", columnList = "user_id"),
                @Index(name = "idx_live_stream_stream_key", columnList = "stream_key"),
                @Index(name = "idx_live_stream_scheduled_at", columnList = "scheduled_at"),
                @Index(name = "idx_live_stream_status", columnList = "status"),
                @Index(name = "idx_live_stream_deleted_at", columnList = "deleted_at")})
@SQLDelete(sql = "UPDATE live_streams SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveStream extends AbstractSoftDeletableEntity {

    /** 配信者（ユーザー）との関連 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 配信タイトル */
    @Column(nullable = false, length = 50)
    private String title;

    /** 配信説明文（任意） */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 一意な配信用ストリームキー */
    @Column(nullable = false, unique = true, length = 255)
    private String streamKey;

    /** 配信のステータス（SCHEDULED / LIVE / ENDED / CANCELLED） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StreamStatus status = StreamStatus.SCHEDULED;

    /** サムネイル画像パス（任意） */
    @Size(max = 1024)
    @Column(length = 1024)
    private String thumbnailPath;

    /** 視聴回数（初期値 0） */
    @Column(nullable = false)
    private Long viewsCount = 0L;

    /** 配信予定日時（予約配信用） */
    private LocalDateTime scheduledAt;

    /** 実際の配信開始時刻 */
    private LocalDateTime startedAt;

    /** 配信終了時刻 */
    private LocalDateTime endedAt;

    // ============================
    // ======== リレーション ========
    // ============================

    /** カテゴリとの関連（論理削除対応） */
    @OneToMany(mappedBy = "liveStream", cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Filter(name = "activeFilter", condition = "deleted_at IS NULL")
    private List<LiveStreamCategory> liveStreamCategories = new ArrayList<>();

    /** タグとの関連（物理削除） */
    @OneToMany(mappedBy = "liveStream", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<LiveStreamTag> liveStreamTags = new ArrayList<>();

    /** チャットメッセージとの関連（物理削除） */
    @OneToMany(mappedBy = "liveStream", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // ===================================================
    // ============= 🏗 コンストラクタ定義 ===============
    // ===================================================

    /**
     * 新規ライブ配信作成用のコンストラクタ。
     *
     * @param title 配信タイトル（null・空不可）
     * @param description 説明文（null可）
     * @param thumbnailPath サムネイルパス（null可）
     * @param scheduledAt 配信予定日時（null不可）
     * @param user 配信ユーザー（null不可）
     * @throws IllegalArgumentException 不正な引数が渡された場合
     */
    public LiveStream(String title, String description, String thumbnailPath,
            LocalDateTime scheduledAt, User user) {
        Assert.hasText(title, "タイトルは必須です");
        Assert.notNull(scheduledAt, "配信予定日時は必須です");
        Assert.notNull(user, "配信ユーザーは必須です");

        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.scheduledAt = scheduledAt;
        this.user = user;
        this.status = StreamStatus.SCHEDULED;
        this.viewsCount = 0L;
        generateStreamKey();
    }

    // ===================================================
    // ============== 🔐 ストリームキー管理 ==============
    // ===================================================

    /**
     * 一意な配信用の streamKey を生成します。
     */
    public void generateStreamKey() {
        this.streamKey = UUID.randomUUID().toString();
    }

    // ===================================================
    // ============ 🎥 ステータス遷移（配信管理） ============
    // ===================================================

    /**
     * 配信を開始します。 ステータスをLIVEに変更し、開始時刻を現在時刻で記録します。
     */
    public void open() {
        this.status = StreamStatus.LIVE;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 配信を終了します。 ステータスをENDEDに変更し、終了時刻を現在時刻で記録します。
     */
    public void close() {
        this.status = StreamStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * 配信をキャンセルします。 ステータスをCANCELLEDに変更し、終了時刻を現在時刻で記録します。
     */
    public void cancel() {
        this.status = StreamStatus.CANCELLED;
        this.endedAt = LocalDateTime.now();
    }

    // ===================================================
    // ============ 📝 メタ情報の更新処理 ============
    // ===================================================

    /**
     * タイトル、説明、サムネイルパスを更新します。null項目はスキップされます。
     *
     * @param title 新しいタイトル（null可）
     * @param description 新しい説明（null可）
     * @param thumbnailPath 新しいサムネイルパス（null可）
     */
    public void updateLiveStreamInfo(String title, String description, String thumbnailPath) {
        if (title != null)
            Assert.hasText(title, "タイトルは空にできません");

        if (title != null)
            this.title = title;
        if (description != null)
            this.description = description;
        if (thumbnailPath != null)
            this.thumbnailPath = thumbnailPath;
    }

    /**
     * 配信予定日時を変更します。
     *
     * @param newScheduledAt 新しい配信予定日時（null不可）
     * @throws IllegalArgumentException newScheduledAtがnullの場合
     */
    public void reschedule(LocalDateTime newScheduledAt) {
        Assert.notNull(newScheduledAt, "新しい配信予定日時は必須です");
        this.scheduledAt = newScheduledAt;
    }
}
