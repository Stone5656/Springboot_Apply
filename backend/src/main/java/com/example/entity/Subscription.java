package com.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import com.example.util.entity.AbstractSoftDeletableEntity;

/**
 * ユーザーのフォロー関係（follower -> target）
 */
@Entity
@Table(
    name = "subscriptions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_subscription_pair",
        columnNames = {"follower_user_id", "target_user_id"}
    ),
    indexes = {
        @Index(name = "idx_subscription_follower", columnList = "follower_user_id"),
        @Index(name = "idx_subscription_target", columnList = "target_user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE subscriptions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
public class Subscription extends AbstractSoftDeletableEntity {

    /** フォローする側のユーザー */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_user_id", nullable = false)
    private User follower;

    /** フォローされる側のユーザー */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User target;

    public Subscription(User follower, User target) {
        this.follower = follower;
        this.target = target;
    }
}
