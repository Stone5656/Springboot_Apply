package com.example.service;

import com.example.dto.videos.*;
import com.example.entity.User;
import com.example.entity.Video;
import com.example.enums.VideoSort;
import com.example.enums.VideoVisibility;
import com.example.repository.VideoRepository;
import com.example.util.CurrentUserUtil;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional(readOnly = true) @RequiredArgsConstructor
public class VideoService {

    private static final String VIDEO_NOT_FOUND = "動画が見つかりません (ID: %s)";
    private static final String VIDEO_IS_DELETED = "動画は削除されています (ID: %s)";

    private final CurrentUserUtil currentUserUtil;
    private final VideoRepository videoRepository;

    // ========================================================
    // ========== 内部ユーティリティメソッド ==================
    // ========================================================

    /**
     * 削除されていない動画をIDから取得。
     *
     * @param id
     *            動画ID
     * @return Videoエンティティ
     * @throws NoSuchElementException
     *             動画が存在しない場合
     * @throws IllegalStateException
     *             動画が削除状態の場合
     */
    private Video getActiveVideoOrThrow(UUID id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(String.format(VIDEO_NOT_FOUND, id)));
        if (video.isDeleted()) {
            throw new IllegalStateException(String.format(VIDEO_IS_DELETED, id));
        }
        return video;
    }

    // ========================================================
    // ========== 動画の作成・取得 ============================
    // ========================================================

    /**
     * 動画を新規作成します。
     *
     * @param request
     *            動画作成リクエストDTO
     * @return 作成された動画のレスポンスDTO
     * @throws IllegalArgumentException
     *             タイトルまたは動画パスがnullの場合
     */
    @Transactional
    public VideoResponseDTO createVideo(VideoCreateRequestDTO request) {
        if (request.getTitle() == null || request.getVideoPath() == null) {
            throw new IllegalArgumentException("タイトルと動画パスは必須です");
        }
        User user = currentUserUtil.getCurrentUser();
        Video video = new Video(request.getTitle(), request.getDescription(), request.getVideoPath(),
                request.getThumbnailPath(), user);
        return VideoResponseDTO.fromEntity(videoRepository.save(video));
    }

    /**
     * 指定IDの動画を取得します。
     *
     * @param id
     *            動画ID
     * @return 該当動画のレスポンスDTO
     * @throws NoSuchElementException
     *             動画が存在しない場合
     */
    public VideoResponseDTO getVideo(UUID id) {
        return VideoResponseDTO.fromEntity(getActiveVideoOrThrow(id));
    }

    /**
     * 現在のユーザーが所有する動画の一覧を取得します。
     *
     * @param pageable
     *            ページ情報
     * @return 動画レスポンスDTOのページ
     */
    public Page<VideoResponseDTO> getMyVideos(Pageable pageable) {
        User user = currentUserUtil.getCurrentUser();
        return videoRepository.findByUserId(user.getId(), pageable).map(VideoResponseDTO::fromEntity);
    }

    // ========================================================
    // ========== 公開動画の検索・取得 =========================
    // ========================================================

    /**
     * 公開動画を検索クエリ付きで取得します。
     *
     * @param request
     *            検索条件
     * @param pageable
     *            ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> searchPublicVideos(SearchRequestDTO request, Pageable pageable) {
        String query = request.getQuery() != null ? request.getQuery() : "";
        Pageable effectivePageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            Sort sort = request.getSortBy() == VideoSort.VIEWS_COUNT
                    ? Sort.by(Sort.Direction.DESC, "viewsCount")
                    : Sort.by(Sort.Direction.DESC, "publishedAt");
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }
        return videoRepository
                .findByTitleContainingIgnoreCaseAndVisibility(query, VideoVisibility.PUBLIC, effectivePageable)
                .map(PublicVideoResponseDTO::fromEntity);
    }

    /**
     * 指定ユーザーの公開動画を取得します。
     *
     * @param userId
     *            ユーザーID
     * @param pageable
     *            ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> getPublicVideosByUser(UUID userId, Pageable pageable) {
        return videoRepository.findByUserIdAndVisibility(userId, VideoVisibility.PUBLIC, pageable)
                .map(PublicVideoResponseDTO::fromEntity);
    }

    /**
     * 人気順に公開動画を取得します。
     *
     * @param pageable
     *            ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> getPopularVideos(Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "viewsCount"));
        return videoRepository.findByVisibility(VideoVisibility.PUBLIC, sorted).map(PublicVideoResponseDTO::fromEntity);
    }

    /**
     * 新着順に公開動画を取得します。
     *
     * @param pageable
     *            ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> getRecentVideos(Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "publishedAt"));
        return videoRepository.findByVisibility(VideoVisibility.PUBLIC, sorted).map(PublicVideoResponseDTO::fromEntity);
    }

    // ========================================================
    // ========== 動画の更新・削除・復元 ========================
    // ========================================================

    /**
     * 動画情報を更新します。
     *
     * @param id
     *            動画ID
     * @param request
     *            更新内容
     * @return 更新後のレスポンスDTO
     * @throws IllegalArgumentException
     *             リクエストがnullの場合
     */
    @Transactional
    public VideoResponseDTO updateVideo(UUID id, VideoUpdateRequestDTO request) {
        Video video = getActiveVideoOrThrow(id);
        if (request == null) {
            throw new IllegalArgumentException("更新内容が指定されていません");
        }
        video.updateVideoInfo(request.getTitle(), request.getDescription(), request.getThumbnailPath(),
                request.getVisibility(), request.getStatus());
        return VideoResponseDTO.fromEntity(video);
    }

    /**
     * 動画を論理削除します。
     *
     * @param id
     *            動画ID
     */
    @Transactional
    public void deleteVideo(UUID id) {
        Video video = getActiveVideoOrThrow(id);
        video.softDelete();
        videoRepository.save(video);
    }

    /**
     * 論理削除された動画を復元します。
     *
     * @param id
     *            動画ID
     * @return 復元された動画のレスポンスDTO
     * @throws NoSuchElementException
     *             動画が存在しない場合
     * @throws IllegalStateException
     *             動画が削除状態でない場合
     */
    @Transactional
    public VideoResponseDTO restoreVideo(UUID id) {
        Video video = videoRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new NoSuchElementException(String.format(VIDEO_NOT_FOUND, id)));
        if (!video.isDeleted()) {
            throw new IllegalStateException("この動画は削除されていません");
        }
        video.restore();
        return VideoResponseDTO.fromEntity(videoRepository.save(video));
    }

    // ========================================================
    // ========== 動画ステータス操作 ===========================
    // ========================================================

    /**
     * 動画の再生数を1増加させます。
     *
     * @param id
     *            動画ID
     */
    @Transactional
    public void incrementViews(UUID id) {
        Video video = getActiveVideoOrThrow(id);
        video.incrementViews();
    }

    /**
     * 動画を公開状態に設定します。
     *
     * @param id
     *            動画ID
     * @param publishedAt
     *            公開日時（null可）
     */
    @Transactional
    public void publishVideo(UUID id, LocalDateTime publishedAt) {
        Video video = getActiveVideoOrThrow(id);
        video.publish(publishedAt);
    }

    /**
     * 動画を非公開状態に戻します。
     *
     * @param id
     *            動画ID
     */
    @Transactional
    public void unpublishVideo(UUID id) {
        Video video = getActiveVideoOrThrow(id);
        video.unpublish();
    }
}
