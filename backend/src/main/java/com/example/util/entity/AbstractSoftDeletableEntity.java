package com.example.util.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

/**
 * 論理削除可能なエンティティ基底クラス。
 * - `deleted_at` によるソフトデリート対応
 */
@MappedSuperclass
@FilterDef(name = "activeFilter")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE {h-table} SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public abstract class AbstractSoftDeletableEntity extends AbstractBaseEntity implements com.example.util.DeletionUtils.Deletable {

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Override
    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public void softDelete() {
        if (!isDeleted()) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    @Override
    public void restore() {
        if (!isDeleted()) {
            throw new IllegalStateException("削除されていないため復元できません");
        }
        this.deletedAt = null;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
