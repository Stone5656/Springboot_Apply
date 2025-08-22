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
 * ライブ配信に対するカテゴリ付けエンティティ。
 */
@Entity
@Table(
    name = "live_stream_categories",
    uniqueConstraints = @UniqueConstraint(name = "uk_live_stream_category", columnNames = {"category_id", "live_stream_id"}),
    indexes = {
        @Index(name = "idx_live_stream_category_category_id", columnList = "category_id"),
        @Index(name = "idx_live_stream_category_live_stream_id", columnList = "live_stream_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE live_stream_categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "activeFilter")
public class LiveStreamCategory extends AbstractSoftDeletableEntity {

    /** 関連カテゴリ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 関連ライブ配信ID */
    @NotNull
    @Column(name = "live_stream_id", nullable = false)
    private UUID liveStreamId;

    // ===================================================
    // =================== コンストラクタ ===================
    // ===================================================

    /**
     * コンストラクタ
     *
     * @param category カテゴリ
     * @param liveStreamId ライブ配信ID
     */
    public LiveStreamCategory(Category category, UUID liveStreamId) {
        this.category = category;
        this.liveStreamId = liveStreamId;
    }
}
