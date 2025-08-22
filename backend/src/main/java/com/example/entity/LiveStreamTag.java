package com.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.util.entity.AbstractBaseEntity;

/**
 * 配信に付与されたタグの中間テーブルエンティティ。
 *
 * @version 1.1
 */
@Entity
@Table(
    name = "live_stream_tags",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_live_stream_tag", columnNames = {"live_stream_id", "tag_id"})
    },
    indexes = {
        @Index(name = "idx_live_stream_tag_stream_id", columnList = "live_stream_id"),
        @Index(name = "idx_live_stream_tag_tag_id", columnList = "tag_id")
    }
)
@Getter
@NoArgsConstructor
public class LiveStreamTag extends AbstractBaseEntity {

    /** 対象配信 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_stream_id", nullable = false)
    private LiveStream liveStream;

    /** タグ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // ====================================================
    // ================= コンストラクタ ===================
    // ====================================================

    /**
     * コンストラクタ
     *
     * @param liveStream 配信
     * @param tag タグ
     */
    public LiveStreamTag(LiveStream liveStream, Tag tag) {
        this.liveStream = liveStream;
        this.tag = tag;
    }

    // ====================================================
    // =================== 業務ロジック ===================
    // ====================================================

    /**
     * 指定されたタグが一致するかを判定します。
     *
     * @param tag 比較対象のタグ
     * @return 一致するなら true
     */
    public boolean isTaggedWith(Tag tag) {
        return this.tag != null && this.tag.equals(tag);
    }

    /**
     * 指定された配信と一致するかを判定します。
     *
     * @param liveStream 比較対象の配信
     * @return 一致するなら true
     */
    public boolean isTaggedTo(LiveStream liveStream) {
        return this.liveStream != null && this.liveStream.equals(liveStream);
    }
}
