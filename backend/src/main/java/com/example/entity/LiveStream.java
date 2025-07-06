package com.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.enums.StreamStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "live_streams", indexes = {
    @Index(name = "idx_live_stream_user_id", columnList = "user_id"),
    @Index(name = "idx_live_stream_stream_key", columnList = "stream_key"),
    @Index(name = "idx_live_stream_scheduled_at", columnList = "scheduled_at")
})
public class LiveStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "stream_key", nullable = false, unique = true, length = 255)
    private String streamKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StreamStatus status = StreamStatus.SCHEDULED;

    @Size(max = 1024)
    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "views_count", nullable = false)
    private Long viewsCount = 0L;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_video_id", nullable = true, unique = true)
    private Video archiveVideo;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
