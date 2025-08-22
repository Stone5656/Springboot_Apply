package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import com.example.util.entity.AbstractSoftDeletableEntity;
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
@Filter(name = "activeFilter")
public class VideoCategory extends AbstractSoftDeletableEntity {

    /** 関連カテゴリ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 関連動画ID */
    @NotNull
    @Column(name = "video_id", nullable = false)
    private UUID videoId;

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
