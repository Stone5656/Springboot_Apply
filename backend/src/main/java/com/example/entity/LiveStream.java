package com.example.entity;

import com.example.enums.StreamStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "live_streams", indexes = {@Index(name = "idx_live_stream_user_id", columnList = "user_id"),
        @Index(name = "idx_live_stream_stream_key", columnList = "stream_key"),
        @Index(name = "idx_live_stream_scheduled_at", columnList = "scheduled_at")})
public class LiveStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 255)
    private String streamKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreamStatus status = StreamStatus.SCHEDULED;

    @Size(max = 1024)
    private String thumbnailPath;

    @Column(nullable = false)
    private Long viewsCount = 0L;

    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ✅ コンストラクタ
    public LiveStream(String title, String description, String thumbnailPath, LocalDateTime scheduledAt, User user) {
        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.scheduledAt = scheduledAt;
        this.user = user;
        this.status = StreamStatus.SCHEDULED;
        this.viewsCount = 0L;
        generateStreamKey();
    }

    // ✅ stream_key自動生成
    public void generateStreamKey() {
        this.streamKey = java.util.UUID.randomUUID().toString();
    }

    // ✅ 配信開始
    public void open() {
        this.status = StreamStatus.LIVE;
        this.startedAt = LocalDateTime.now();
    }

    // ✅ 配信終了
    public void close() {
        this.status = StreamStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    // ✅ 配信キャンセル
    public void cancel() {
        this.status = StreamStatus.CANCELLED;
        this.endedAt = LocalDateTime.now();
    }

    // ✅ 配信予約日時更新
    public void reschedule(LocalDateTime newScheduledAt) {
        if (newScheduledAt != null) {
            this.scheduledAt = newScheduledAt;
        }
    }

    // ✅ 一括情報更新（既存）
    public void updateLiveStreamInfo(String title, String description, String thumbnailPath) {
        if (title != null)
            this.title = title;
        if (description != null)
            this.description = description;
        if (thumbnailPath != null)
            this.thumbnailPath = thumbnailPath;
    }
}
