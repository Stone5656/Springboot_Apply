package com.example.enums;

/**
 * 動画処理の状態を表す列挙型。 アップロードから公開、削除までの全状態をカバーする。
 */
public enum VideoStatus {

    /** アップロード直後の未処理状態 */
    UPLOADED,

    /** エンコード処理中 */
    ENCODING,

    /** エンコード成功後、公開前の確認段階 */
    READY,

    /** エンコードまたは処理に失敗した状態 */
    FAILED,

    /** 論理削除済み */
    DELETED;

    /**
     * 公開判定に使用可能な補助メソッド。 READY 以外は公開不可。
     *
     * @return 公開可能な状態か
     */
    public boolean isPublishable()
    {
        return this == READY;
    }

    /**
     * エラー状態かを判定。
     *
     * @return FAILED または DELETED の場合 true
     */
    public boolean isErrorOrDeleted()
    {
        return this == FAILED || this == DELETED;
    }
}
// [UPLOADED] → [ENCODING] → [READY] → 公開
// ↓
// [FAILED]（失敗時）
// ↓
// [DELETED]（論理削除）
