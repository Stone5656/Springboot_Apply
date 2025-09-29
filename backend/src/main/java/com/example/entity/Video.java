package com.example.entity;

import com.example.enums.VideoStatus;
import com.example.enums.VideoVisibility;
import com.example.util.entity.AbstractSoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.springframework.util.Assert;

/**
 * 動画情報を表すエンティティ。 ユーザーとの関連、メタ情報、公開範囲、状態、削除などを包括的に管理する。
 *
 * @version 1.1
 */
@Entity
@Table(name = "videos",
        indexes = {@Index(name = "idx_video_user_id", columnList = "user_id"),
                @Index(name = "idx_video_published_at", columnList = "published_at"),
                @Index(name = "idx_video_status_visibility", columnList = "status, visibility"),
                @Index(name = "idx_video_deleted_at", columnList = "deleted_at")})
@SQLDelete(
        sql = "UPDATE videos SET deleted_at = CURRENT_TIMESTAMP, status = 'DELETED' WHERE id = ?")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends AbstractSoftDeletableEntity {

    /** 投稿ユーザー。遅延ロード。 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** タイトル（最大50文字、必須） */
    @Size(max = 50)
    @NotBlank
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    /** 説明文（最大2000文字、任意） */
    @Size(max = 2000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 動画ファイルの保存パス（最大1024文字、必須） */
    @Size(max = 1024)
    @NotBlank
    @Column(name = "video_path", nullable = false, length = 1024)
    private String videoPath;

    /** サムネイル画像のパス（最大1024文字、任意） */
    @Size(max = 1024)
    @Column(name = "thumbnail_path", length = 1024)
    private String thumbnailPath;

    /** 動画の再生時間（秒単位） */
    @Min(0)
    @Column(name = "duration")
    private Integer duration;

    /** 公開範囲（PUBLIC / PRIVATE） */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 50)
    private VideoVisibility visibility = VideoVisibility.PRIVATE;

    /** 動画の状態（UPLOADED / ENCODING / PUBLISHED / DELETED） */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private VideoStatus status = VideoStatus.UPLOADED;

    /** 再生回数（初期値0） */
    @Min(0)
    @Column(name = "views_count", nullable = false)
    private Long viewsCount = 0L;

    /** 動画URLスラッグ（SEO対応等、ユニーク） */
    @Column(name = "slug", length = 255, unique = true)
    private String slug;

    /** 公開日時（未公開の場合null） */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // ============================
    // ======== リレーション ========
    // ============================

    /** カテゴリとの関連（論理削除対応） */
    @OneToMany(
        mappedBy = "video",
        cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Filter(name = "activeFilter", condition = "deleted_at IS NULL")
    private List<VideoCategory> videoCategories = new ArrayList<>();

    /** タグとの関連（物理削除） */
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<VideoTag> videoTags = new ArrayList<>();

    // ===================================================
    // ============= 🔧 コンストラクタ定義 =============
    // ===================================================

    /**
     * 新規作成用コンストラクタ。
     *
     * @param title タイトル（null・空不可）
     * @param description 説明文（null可）
     * @param videoPath 動画ファイルの保存パス（null・空不可）
     * @param thumbnailPath サムネイル画像のパス（null可）
     * @param user 投稿ユーザー（null不可）
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    public Video(String title, String description, String videoPath, String thumbnailPath,
            User user) {
        Assert.hasText(title, "タイトルは必須です");
        Assert.hasText(videoPath, "動画パスは必須です");
        Assert.notNull(user, "ユーザーは必須です");

        this.title = title;
        this.description = description;
        this.videoPath = videoPath;
        this.thumbnailPath = thumbnailPath;
        this.user = user;
        this.status = VideoStatus.UPLOADED;
        this.visibility = VideoVisibility.PRIVATE;
        this.viewsCount = 0L;
    }

    // ===================================================
    // ============= 🛠 メタ情報更新メソッド =============
    // ===================================================

    /**
     * メタ情報の更新（nullを無視）。
     *
     * @param title タイトル
     * @param description 説明文
     * @param thumbnailPath サムネイルパス
     * @param visibility 公開範囲
     * @param status ステータス
     */
    public void updateVideoInfo(String title, String description, String thumbnailPath,
            VideoVisibility visibility, VideoStatus status) {
        if (title != null)
            Assert.hasText(title, "タイトルは空にできません");
        if (thumbnailPath != null)
            Assert.hasText(thumbnailPath, "サムネイルパスは空にできません");

        if (title != null)
            this.title = title;
        if (description != null)
            this.description = description;
        if (thumbnailPath != null)
            this.thumbnailPath = thumbnailPath;
        if (visibility != null)
            this.visibility = visibility;
        if (status != null)
            this.status = status;
    }

    // ===================================================
    // ============ 📈 再生・公開制御メソッド ============
    // ===================================================

    /**
     * 再生数を1増やす。
     */
    public void incrementViews() {
        this.viewsCount += 1;
    }

    /**
     * 動画を公開する。
     *
     * @param publishedAt 公開日時（nullなら現在時刻）
     */
    public void publish(LocalDateTime publishedAt) {
        this.visibility = VideoVisibility.PUBLIC;
        this.publishedAt = (publishedAt != null) ? publishedAt : LocalDateTime.now();
    }

    /**
     * 動画を非公開にする。
     */
    public void unpublish() {
        this.visibility = VideoVisibility.PRIVATE;
        this.publishedAt = null;
    }

    // ===================================================
    // ============== 🗑 論理削除・復元関連 ==============
    // ===================================================

    /**
     * 論理削除。ステータスをDELETEDに、削除日時を現在に。
     */
    @Override
    public void softDelete() {
        super.softDelete();
        this.status = VideoStatus.DELETED;
    }

    /**
     * 論理削除された動画を復元。
     *
     * @throws IllegalStateException 削除状態でない場合
     */
    @Override
    public void restore() {
        super.restore();
        this.status = VideoStatus.UPLOADED;
    }

    // ===================================================
    // =============== 📣 ステータス判定 ================
    // ===================================================

    /**
     * 動画が公開中かどうかを判定。
     *
     * @return 公開中なら true
     */
    public boolean isPublic() {
        return VideoVisibility.PUBLIC.equals(this.visibility) && deletedAt == null;
    }

    // ===================================================
    // ============ ⚠ 整合性チェックコールバック =========
    // ===================================================

    /**
     * 削除状態と削除日時の整合性を確認。
     *
     * @throws IllegalStateException 状態と削除日時が不一致な場合
     */
    @PreUpdate
    public void validateStateConsistency() {
        if ((status == VideoStatus.DELETED) != (deletedAt != null)) {
            throw new IllegalStateException("削除状態とdeletedAtが一致していません");
        }
    }
}
