package com.example.util;

import java.util.List;

/**
 * 論理削除および復元に関するユーティリティ。
 * 対象のエンティティは {@link Deletable} インターフェースを実装している必要があります。
 */
public class DeletionUtils {

    private DeletionUtils() {
        // インスタンス化禁止
    }

    /**
     * 論理削除可能なエンティティのリストに対して、未削除のものだけを softDelete する。
     *
     * @param entities List of soft-deletable entities
     * @param <T> Deletableを実装したエンティティ
     */
    public static <T extends Deletable> void softDeleteEntities(List<T> entities) {
        if (entities == null) return;
        entities.forEach(e -> {
            if (!e.isDeleted()) {
                e.softDelete();
            }
        });
    }

    /**
     * 論理削除されたエンティティのリストに対して、削除済みのものだけを restore する。
     *
     * @param entities List of deletable entities
     * @param <T> Deletableを実装したエンティティ
     */
    public static <T extends Deletable> void restoreEntities(List<T> entities) {
        if (entities == null) return;
        entities.forEach(e -> {
            if (e.isDeleted()) {
                e.restore();
            }
        });
    }

    /**
     * 論理削除／復元対象の共通インターフェース。
     * {@code isDeleted()}, {@code softDelete()}, {@code restore()} の実装が必要です。
     */
    public interface Deletable {
        boolean isDeleted();
        void softDelete();
        void restore();
    }
}
