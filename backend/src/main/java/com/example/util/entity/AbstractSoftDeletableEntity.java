package com.example.util.entity;

import com.example.util.DeletionUtils.Deletable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

/**
 * 論理削除機能を提供する抽象エンティティ。
 * このクラスを継承することで deleted_at カラムおよび isDeleted/softDelete/restore を統一的に実装可能。
 *
 * @see com.example.util.DeletionUtils.Deletable
 */
@MappedSuperclass
@FilterDef(name = "activeFilter")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE {h-table} SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public abstract class AbstractSoftDeletableEntity implements Deletable {

    /** 論理削除された日時。nullであれば有効。 */
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
            throw new IllegalStateException("既に削除されていません");
        }
        this.deletedAt = null;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
