package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import com.example.util.entity.AbstractSoftDeletableEntity;

@Entity
@Table(
    name = "subscriptions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_subscriptions_subscriber_target",
        columnNames = {"subscriber_id", "target_id"}
    ),
    indexes = {
        @Index(name = "idx_subscriptions_subscriber", columnList = "subscriber_id"),
        @Index(name = "idx_subscriptions_target", columnList = "target_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE subscriptions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
public class Subscription extends AbstractSoftDeletableEntity {

    /** フォローする側（= 自分） */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    /** フォローされる側（= 相手） */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Builder
    public Subscription(User subscriber, User target) {
        this.subscriber = subscriber;
        this.target = target;
    }
}
