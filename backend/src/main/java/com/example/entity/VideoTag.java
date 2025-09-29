package com.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.util.entity.AbstractBaseEntity;

/**
 * 動画に付与されたタグの中間テーブルエンティティ。
 *
 * @version 1.1
 */
@Entity
@Table(
    name = "video_tags",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_video_tag", columnNames = {"video_id", "tag_id"})
    },
    indexes = {
        @Index(name = "idx_video_tag_video_id", columnList = "video_id"),
        @Index(name = "idx_video_tag_tag_id", columnList = "tag_id")
    }
)
@Getter
@NoArgsConstructor
public class VideoTag extends AbstractBaseEntity {

    /** 対象動画 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

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
     * @param video 動画
     * @param tag タグ
     */
    public VideoTag(Video video, Tag tag) {
        this.video = video;
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
     * 指定された動画と一致するかを判定します。
     *
     * @param video 比較対象の動画
     * @return 一致するなら true
     */
    public boolean isTaggedTo(Video video) {
        return this.video != null && this.video.equals(video);
    }
}
