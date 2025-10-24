package com.example.repository;

import com.example.entity.VideoCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface VideoCategoryRepository extends JpaRepository<VideoCategory, UUID> {

  // 取得
  List<VideoCategory> findByVideo_Id(UUID videoId);
  List<VideoCategory> findByCategory_Id(UUID categoryId);

  // 削除
  @Modifying int deleteByVideo_Id(UUID videoId);
  @Modifying int deleteByCategory_Id(UUID categoryId);
  @Modifying int deleteByVideo_IdAndCategory_Id(UUID videoId, UUID categoryId);

  // 既存チェック（必要なら）
  boolean existsByVideo_IdAndCategory_Id(UUID videoId, UUID categoryId);
}
