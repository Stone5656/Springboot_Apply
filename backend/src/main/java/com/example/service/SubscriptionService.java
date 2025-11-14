package com.example.service;

import com.example.dto.subscriptions.*;
import com.example.entity.Subscription;
import com.example.entity.User;
import com.example.repository.SubscriptionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ユーザー間フォロー（Subscription）用サービス.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @PersistenceContext
    private EntityManager em;

    // ========================================================
    // =============== Ⅰ. 参照系（Public/Authenticated） ======
    // ========================================================

    /**
     * 指定ユーザーがフォローしている対象一覧を取得.
     */
    public List<SubscriptionResponseDTO> getSubscriptionsBySubscriber(UUID subscriberId) {
        List<Subscription> list = subscriptionRepository.findBySubscriber_Id(subscriberId);
        return list.stream()
                .map(SubscriptionResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 指定ユーザーをフォローしているユーザー一覧を取得.
     */
    public List<SubscriptionResponseDTO> getSubscriptionsByTarget(UUID targetId) {
        List<Subscription> list = subscriptionRepository.findByTarget_Id(targetId);
        return list.stream()
                .map(SubscriptionResponseDTO::fromEntity)
                .toList();
    }

    // ========================================================
    // ============ Ⅱ. 更新系（Authenticated想定） ============
    // ========================================================

    /**
     * フォロー登録.
     *
     * 本来は subscriberId は SecurityContext から取得して、
     * req.getSubscriberId() は一致チェックに使う方が安全。
     */
    @Transactional
    public SubscriptionResponseDTO createSubscription(SubscriptionCreateRequestDTO req) {
        UUID subscriberId = req.getSubscriberId();
        UUID targetId = req.getTargetId();

        if (subscriberId == null) {
            throw new IllegalArgumentException("subscriberId は必須です（実運用では認証情報から補完してください）。");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("targetId は必須です。");
        }
        if (subscriberId.equals(targetId)) {
            throw new IllegalArgumentException("自分自身をフォローすることはできません。");
        }

        // 既存チェック（ユニーク制約と二重防御）
        if (subscriptionRepository.existsBySubscriber_IdAndTarget_Id(subscriberId, targetId)) {
            throw new IllegalStateException("既にフォロー済みです。");
        }

        User subscriberRef = em.getReference(User.class, subscriberId);
        User targetRef = em.getReference(User.class, targetId);

        Subscription entity = Subscription.builder()
                .subscriber(subscriberRef)
                .target(targetRef)
                .build();

        Subscription saved = subscriptionRepository.save(entity);
        return SubscriptionResponseDTO.fromEntity(saved);
    }

    /**
     * フォロー解除.
     *
     * 失敗しても例外を投げず「なかったものとして扱う」実装。
     */
    @Transactional
    public void removeSubscription(SubscriptionRemoveRequestDTO req) {
        UUID subscriberId = req.getSubscriberId();
        UUID targetId = req.getTargetId();

        if (subscriberId == null || targetId == null) {
            throw new IllegalArgumentException("subscriberId と targetId は必須です。");
        }

        subscriptionRepository.deleteBySubscriber_IdAndTarget_Id(subscriberId, targetId);
    }

    /**
     * 退会・強制退会などで、ユーザーに紐づくフォロー関係を一括削除したい場合のユーティリティ.
     */
    @Transactional
    public void deleteAllForUser(UUID userId) {
        // 自分がフォローしている関係
        subscriptionRepository.deleteBySubscriber_Id(userId);
        // 自分をフォローしている関係
        subscriptionRepository.deleteByTarget_Id(userId);
    }
}
