package com.example.repository;

import com.example.entity.Video;
import com.example.enums.VideoVisibility;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    Page<Video> findByUserIdAndVisibility(UUID userId, VideoVisibility visibility, Pageable pageable);

    Page<Video> findByTitleContainingIgnoreCaseAndVisibility(String title, VideoVisibility visibility,
            Pageable pageable);

    Page<Video> findByUserId(UUID userId, Pageable pageable);

    Page<Video> findByVisibility(VideoVisibility visibility, Pageable pageable);

    // --- 削除済み含めて取得 ---
    @Query("SELECT v FROM Video v WHERE v.id = :id")
    Optional<Video> findByIdIncludingDeleted(@Param("id")
    UUID id);

    // --- 論理削除の復元 ---
    @Modifying @Query("UPDATE Video v SET v.deletedAt = NULL, v.status = 'READY' WHERE v.id = :id")
    void restoreById(@Param("id")
    UUID id);
}
