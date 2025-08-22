package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.util.entity.AbstractBaseEntity;

/**
 * タグのメタ情報を保持するエンティティ。
 * 動画やライブ配信に付与されるカテゴリ的なラベル。
 *
 * @version 2.1
 */
@Entity
@Table(
    name = "tags",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tag_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_tag_slug", columnNames = "slug")
    },
    indexes = {
        @Index(name = "idx_tag_name", columnList = "name"),
        @Index(name = "idx_tag_slug", columnList = "slug")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends AbstractBaseEntity {

    /** タグの名前（画面表示用） */
    @Column(name = "name", nullable = false, length = 255)
    @Size(max = 255)
    private String name;

    /** タグのスラッグ（URLや検索用） */
    @Column(name = "slug", nullable = false, length = 255)
    @Size(max = 255)
    private String slug;

    // ====================================================
    // ================= コンストラクタ ===================
    // ====================================================

    /**
     * Tagエンティティの唯一のコンストラクタ
     *
     * @param name タグ名
     * @param slug タグスラッグ
     */
    public Tag(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    // ====================================================
    // =================== 業務ロジック ===================
    // ====================================================

    /**
     * タグのスラッグが一致するかを判定します。
     *
     * @param slug 比較対象のスラッグ
     * @return 一致する場合はtrue
     */
    public boolean isSlug(String slug) {
        return this.slug.equalsIgnoreCase(slug);
    }

    /**
     * 指定されたタグ名が一致するかを判定します。
     *
     * @param name 比較対象のタグ名
     * @return 一致する場合はtrue
     */
    public boolean hasName(String name) {
        return this.name.equalsIgnoreCase(name);
    }
}
