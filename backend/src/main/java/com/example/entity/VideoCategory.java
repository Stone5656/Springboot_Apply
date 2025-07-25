package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.util.entity.AbstractSoftDeletableEntity;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 動画に対するカテゴリ付けエンティティ。
 */
@Entity
@Table(
    name = "video_categories",
    uniqueConstraints = @UniqueConstraint(name = "uk_video_category", columnNames = {"category_id", "video_id"}),
    indexes = {
        @Index(name = "idx_video_category_category_id", columnList = "category_id"),
        @Index(name = "idx_video_category_video_id", columnList = "video_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE video_categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@FilterDef(name = "activeVideoCategoryFilter")
@Filter(name = "activeVideoCategoryFilter", condition = "deleted_at IS NULL")
public class VideoCategory extends AbstractSoftDeletableEntity {

    /** 主キーUUID（BINARY(16)） */
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    /** 関連カテゴリ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 関連動画ID */
    @NotNull
    @Column(name = "video_id", nullable = false)
    private UUID videoId;

    /** 作成日時 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新日時 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===================================================
    // ================== コンストラクタ ==================
    // ===================================================

    /**
     * コンストラクタ
     *
     * @param category カテゴリ
     * @param videoId 動画ID
     */
    public VideoCategory(Category category, UUID videoId) {
        this.category = category;
        this.videoId = videoId;
    }
}
