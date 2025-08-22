package com.example.entity;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.*;
import com.example.util.entity.AbstractSoftDeletableEntity;

/**
 * フォロー関係を表すエンティティ。
 * follower_user_id が following_user_id をフォローしている関係を保持。
 *
 * @version 2.0
 */
@Entity
@Table(
    name = "subscriptions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_user_id", "following_user_id"}),
    indexes = {
        @Index(name = "idx_subscriptions_follower", columnList = "follower_user_id"),
        @Index(name = "idx_subscriptions_following", columnList = "following_user_id")
    }
)
@Getter
@SQLDelete(sql = "UPDATE subscriptions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND deleted_at IS NULL")
@Filter(name = "activeFilter")
public class Subscription extends AbstractSoftDeletableEntity {

    /** フォローするユーザーID（フォロワー） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_user_id", nullable = false)
    private User follower;

    /** フォローされるユーザーID（フォロー対象） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_user_id", nullable = false)
    private User following;

    // ====================================================
    // ================= コンストラクタ ===================
    // ====================================================

    /**
     * Subscription エンティティの唯一のコンストラクタ
     *
     * @param follower フォロワー
     * @param following フォロー対象
     */
    public Subscription(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    // ====================================================
    // =================== 業務ロジック ===================
    // ====================================================

    /**
     * 指定ユーザーがこのフォロー関係のフォロワーかどうかを確認します。
     *
     * @param user ユーザー
     * @return フォロワーであれば true
     */
    public boolean isFollower(User user) {
        return this.follower != null && this.follower.equals(user);
    }

    /**
     * 指定ユーザーがこのフォロー関係のフォロー対象かどうかを確認します。
     *
     * @param user ユーザー
     * @return フォロー対象であれば true
     */
    public boolean isFollowing(User user) {
        return this.following != null && this.following.equals(user);
    }
}
