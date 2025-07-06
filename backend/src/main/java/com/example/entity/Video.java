package com.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.enums.VideoStatus;
import com.example.enums.VideoVisibility;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "videos", indexes = {
    @Index(name = "idx_video_user_id", columnList = "user_id"),
    @Index(name = "idx_video_published_at", columnList = "published_at")
})
public class Video {

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

    @Size(max = 1024)
    @Column(name = "video_path", nullable = false)
    private String videoPath;

    @Size(max = 1024)
    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "duration")
    private Integer duration; // 秒数

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 50)
    private VideoVisibility visibility = VideoVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private VideoStatus status = VideoStatus.UPLOADED;

    @Column(name = "views_count", nullable = false)
    private Long viewsCount = 0L;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ 新規作成用コンストラクタ
    public Video(String title, String description, String videoPath, String thumbnailPath, User user) {
        this.title = title;
        this.description = description;
        this.videoPath = videoPath;
        this.thumbnailPath = thumbnailPath;
        this.user = user;
        this.status = VideoStatus.UPLOADED;
        this.visibility = VideoVisibility.PRIVATE;
        this.viewsCount = 0L;
    }

    // ✅ 動画情報の更新（部分更新OK）
    public void updateVideoInfo(String title, String description, String thumbnailPath, VideoVisibility visibility, VideoStatus status) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (thumbnailPath != null) this.thumbnailPath = thumbnailPath;
        if (visibility != null) this.visibility = visibility;
        if (status != null) this.status = status;
    }

    // ✅ 再生数を1増やす
    public void incrementViews() {
        this.viewsCount += 1;
    }

    // ✅ 公開する
    public void publish(LocalDateTime publishedAt) {
        this.visibility = VideoVisibility.PUBLIC;
        this.publishedAt = publishedAt != null ? publishedAt : LocalDateTime.now();
    }

    // ✅ 非公開にする
    public void unpublish() {
        this.visibility = VideoVisibility.PRIVATE;
        this.publishedAt = null;
    }

}
