package com.example.repository;

import com.example.entity.LiveStreamCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface LiveStreamCategoryRepository extends JpaRepository<LiveStreamCategory, UUID> {

  // 取得
  List<LiveStreamCategory> findByLiveStream_Id(UUID liveStreamId);
  List<LiveStreamCategory> findByCategory_Id(UUID categoryId);

  // 削除
  @Modifying int deleteByLiveStream_Id(UUID liveStreamId);
  @Modifying int deleteByCategory_Id(UUID categoryId);
  @Modifying int deleteByLiveStream_IdAndCategory_Id(UUID liveStreamId, UUID categoryId);

  // 既存チェック（必要なら）
  boolean existsByLiveStream_IdAndCategory_Id(UUID liveStreamId, UUID categoryId);
}
