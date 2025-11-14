package com.example.repository;

import com.example.entity.VideoTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

/**
 * VideoTag 中間テーブル用リポジトリ。
 */
public interface VideoTagRepository extends JpaRepository<VideoTag, UUID> {

    // 取得
    List<VideoTag> findByVideo_Id(UUID videoId);
    List<VideoTag> findByTag_Id(UUID tagId);

    // 削除
    @Modifying
    int deleteByVideo_Id(UUID videoId);

    @Modifying
    int deleteByTag_Id(UUID tagId);

    @Modifying
    int deleteByVideo_IdAndTag_Id(UUID videoId, UUID tagId);

    // 既存チェック
    boolean existsByVideo_IdAndTag_Id(UUID videoId, UUID tagId);
}
