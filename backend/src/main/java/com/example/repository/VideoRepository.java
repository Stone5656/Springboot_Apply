package com.example.repository;

import com.example.entity.Video;
import com.example.enums.VideoVisibility;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    // ✅ 特定のユーザーの公開動画一覧（ページング）
    Page<Video> findByUserIdAndVisibility(UUID userId, VideoVisibility visibility, Pageable pageable);

    // ✅ タイトル部分一致 + 公開動画（ページング・ソート対応）
    Page<Video> findByTitleContainingIgnoreCaseAndVisibility(String title, VideoVisibility visibility,
            Pageable pageable);

    Page<Video> findByUserId(UUID userId, Pageable pageable);

    // ✅ 全公開動画一覧（ページング・ソート）
    Page<Video> findByVisibility(VideoVisibility visibility, Pageable pageable);
}
