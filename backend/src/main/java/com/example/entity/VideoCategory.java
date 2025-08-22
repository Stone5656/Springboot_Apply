package com.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import com.example.util.entity.AbstractSoftDeletableEntity;

/**
 * 動画に対するカテゴリ付けエンティティ。
 */
@Entity
@Table(name = "video_categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_video_category",
                columnNames = {"category_id", "video_id"}),
        indexes = {@Index(name = "idx_video_category_category_id", columnList = "category_id"),
                @Index(name = "idx_video_category_video_id", columnList = "video_id")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE video_categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
public class VideoCategory extends AbstractSoftDeletableEntity {

    /** 関連カテゴリ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 関連動画ID */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    // ===================================================
    // ================== コンストラクタ ==================
    // ===================================================

    /**
     * コンストラクタ
     *
     * @param category カテゴリ
     * @param video 動画ID
     */
    public VideoCategory(Category category, Video video) {
        this.category = category;
        this.video = video;
    }
}
