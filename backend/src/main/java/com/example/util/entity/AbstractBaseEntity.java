package com.example.util.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * IDおよびタイムスタンプ付きの基底エンティティ。
 * - UUID主キー
 * - created_at / updated_at の自動管理
 */
@MappedSuperclass
public abstract class AbstractBaseEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    protected UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
