package com.example.repository;

import com.example.entity.Subscription;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

/**
 * Subscription（ユーザー間フォロー）用リポジトリ
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /** あるユーザーがフォローしている一覧 */
    List<Subscription> findBySubscriber_Id(UUID subscriberId);

    /** あるユーザーをフォローしている一覧 */
    List<Subscription> findByTarget_Id(UUID targetId);

    /** フォロー関係が既に存在するか（重複登録防止） */
    boolean existsBySubscriber_IdAndTarget_Id(UUID subscriberId, UUID targetId);

    /** 特定のフォロー関係を解除 */
    @Modifying
    int deleteBySubscriber_IdAndTarget_Id(UUID subscriberId, UUID targetId);

    /** あるユーザーが「フォローしている」関係を全部消す（退会時など） */
    @Modifying
    int deleteBySubscriber_Id(UUID subscriberId);

    /** あるユーザーを「フォローしている」関係を全部消す（退会時など） */
    @Modifying
    int deleteByTarget_Id(UUID targetId);
}
