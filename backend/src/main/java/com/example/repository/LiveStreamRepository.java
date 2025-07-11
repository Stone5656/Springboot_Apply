package com.example.repository;

import com.example.entity.LiveStream;
import com.example.enums.StreamStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveStreamRepository extends JpaRepository<LiveStream, Long> {
    Optional<LiveStream> findByStreamKey(String streamKey);

    Page<LiveStream> findByUserId(UUID userId, Pageable pageable);

    Page<LiveStream> findByUserIdAndStatus(UUID userId, StreamStatus status, Pageable pageable);

    Page<LiveStream> findByStatus(StreamStatus status, Pageable pageable);

    Page<LiveStream> findByStatusIn(List<StreamStatus> statuses, Pageable pageable);

    Page<LiveStream> findByTitleContainingIgnoreCaseAndStatus(String title, StreamStatus status, Pageable pageable);
}
