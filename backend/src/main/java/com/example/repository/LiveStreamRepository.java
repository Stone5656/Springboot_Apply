package com.example.repository;

import com.example.entity.LiveStream;
import com.example.enums.StreamStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LiveStreamRepository extends JpaRepository<LiveStream, UUID> {

    Optional<LiveStream> findByStreamKey(String streamKey);

    Page<LiveStream> findByUserId(UUID userId, Pageable pageable);

    Page<LiveStream> findByUserIdAndStatus(UUID userId, StreamStatus status, Pageable pageable);

    Page<LiveStream> findByStatus(StreamStatus status, Pageable pageable);

    Page<LiveStream> findByStatusIn(List<StreamStatus> statuses, Pageable pageable);

    Page<LiveStream> findByTitleContainingIgnoreCaseAndStatus(String title, StreamStatus status, Pageable pageable);

    // --- 削除済み含めて取得 ---
    @Query("SELECT l FROM LiveStream l WHERE l.id = :id")
    Optional<LiveStream> findByIdIncludingDeleted(@Param("id")
    UUID id);

    // --- 論理削除の復元 ---
    @Modifying @Query("UPDATE LiveStream l SET l.deletedAt = NULL, l.status = 'SCHEDULED' WHERE l.id = :id")
    void restoreById(@Param("id")
    UUID id);
}
