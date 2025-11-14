package com.example.repository;

import com.example.entity.LiveStreamTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

/**
 * LiveStreamTag 中間テーブル用リポジトリ。
 */
public interface LiveStreamTagRepository extends JpaRepository<LiveStreamTag, UUID> {

    // 取得
    List<LiveStreamTag> findByLiveStream_Id(UUID liveStreamId);
    List<LiveStreamTag> findByTag_Id(UUID tagId);

    // 削除
    @Modifying
    int deleteByLiveStream_Id(UUID liveStreamId);

    @Modifying
    int deleteByTag_Id(UUID tagId);

    @Modifying
    int deleteByLiveStream_IdAndTag_Id(UUID liveStreamId, UUID tagId);

    // 既存チェック
    boolean existsByLiveStream_IdAndTag_Id(UUID liveStreamId, UUID tagId);
}
