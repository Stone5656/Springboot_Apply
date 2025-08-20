package com.example.entity;

import static com.example.util.DeletionUtils.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;
import com.example.util.entity.AbstractSoftDeletableEntity;
import java.util.List;

/**
 * 動画や配信のカテゴリを表すエンティティ。
 *
 * @version 2.0
 */
@Entity
@Table(
    name = "categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_category_slug", columnNames = "slug")
    },
    indexes = {
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_category_slug", columnList = "slug")
    }
)
@Getter
@SQLDelete(sql = "UPDATE categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@FilterDef(name = "activeFilter")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
public class Category extends AbstractSoftDeletableEntity {

    // ==========================
    // ======== カラム定義 ========
    // ==========================

    /** カテゴリ名 */
    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** スラッグ（URL・検索などで利用） */
    @NotBlank
    @Size(max = 255)
    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    /** 説明文（任意） */
    @Size(max = 2000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ============================
    // ======== リレーション ========
    // ============================

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Filter(name = "activeFilter")
    private List<VideoCategory> videoCategories;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Filter(name = "activeFilter")
    private List<LiveStreamCategory> liveStreamCategories;

    // ====================================================
    // ================= コンストラクタ ===================
    // ====================================================

    /**
     * Categoryエンティティの唯一のコンストラクタ
     *
     * @param name カテゴリ名
     * @param slug スラッグ
     * @param description 説明文
     */
    public Category(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    // ====================================================
    // =================== 業務ロジック ===================
    // ====================================================

    /**
     * 指定された名前と一致するかを判定します。
     *
     * @param otherName 比較対象の名前
     * @return true = 一致
     */
    public boolean hasName(String otherName) {
        return this.name.equalsIgnoreCase(otherName);
    }

    /**
     * 指定されたスラッグと一致するかを判定します。
     *
     * @param otherSlug 比較対象のスラッグ
     * @return true = 一致
     */
    public boolean hasSlug(String otherSlug) {
        return this.slug.equalsIgnoreCase(otherSlug);
    }

    // ===================================================
    // ============== ♻ 論理削除・復元処理 ==============
    // ===================================================

    /**
     * このエンティティを論理削除状態にします。
     * すでに削除済みであれば何もしません。
     * カテゴリとの関連がある場合はカスケードで子要素も削除します。
     */
    @Override
    public void softDelete() {
        if (isDeleted()) return;
        super.softDelete(); // 親で共通処理
        softDeleteEntities(videoCategories);
        softDeleteEntities(liveStreamCategories);
    }

    /**
     * 論理削除された配信を復元します。
     *
     * @throws IllegalStateException 削除されていない配信を復元しようとした場合
     */
    @Override
    public void restore() {
        super.restore();
        restoreEntities(videoCategories);
        restoreEntities(liveStreamCategories);
    }
}
