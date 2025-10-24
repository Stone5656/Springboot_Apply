package com.example.service;

import com.example.dto.videos.*;
import com.example.entity.User;
import com.example.entity.Video;
import com.example.enums.VideoSort;
import com.example.enums.VideoVisibility;
import com.example.repository.UserRepository;
import com.example.repository.VideoRepository;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoService {

    private static final String VIDEO_NOT_FOUND  = "動画が見つかりません (ID: %s)";
    private static final String VIDEO_IS_DELETED = "動画は削除されています (ID: %s)";

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    // ========================================================
    // ========== 内部ユーティリティメソッド ==================
    // ========================================================

    /**
     * 削除されていない動画をIDから取得します。
     *
     * @param id 動画ID
     * @return Videoエンティティ
     * @throws NoSuchElementException 動画が存在しない場合
     * @throws IllegalStateException  動画が削除状態の場合
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
    // ========== Ⅰ. 未認証OK（Public APIs） ==================
    // ========================================================

    /**
     * 指定IDの動画を取得します（公開/非公開の可視性は呼び出し側で制御してください）。
     *
     * @param id 動画ID
     * @return 該当動画のレスポンスDTO
     * @throws NoSuchElementException 動画が存在しない場合
     */
    public VideoResponseDTO getVideo(UUID id) {
        return VideoResponseDTO.fromEntity(getActiveVideoOrThrow(id));
    }

    /**
     * 公開動画を検索クエリ付きで取得します。
     *
     * @param request  検索条件
     * @param pageable ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> searchPublicVideos(SearchRequestDTO request, Pageable pageable) {
        String query = request.getQuery() != null ? request.getQuery() : "";
        Pageable effectivePageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            Sort sort = (request.getSortBy() == VideoSort.VIEWS_COUNT)
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
     * @param userId   ユーザーID
     * @param pageable ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> getPublicVideosByUser(UUID userId, Pageable pageable) {
        return videoRepository
            .findByUserIdAndVisibility(userId, VideoVisibility.PUBLIC, pageable)
            .map(PublicVideoResponseDTO::fromEntity);
    }

    /**
     * 人気順に公開動画を取得します。
     *
     * @param pageable ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> getPopularVideos(Pageable pageable) {
        Pageable sorted = PageRequest.of(
            pageable.getPageNumber(), pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "viewsCount")
        );
        return videoRepository.findByVisibility(VideoVisibility.PUBLIC, sorted)
                              .map(PublicVideoResponseDTO::fromEntity);
    }

    /**
     * 新着順に公開動画を取得します。
     *
     * @param pageable ページ情報
     * @return 公開動画レスポンスDTOのページ
     */
    public Page<PublicVideoResponseDTO> getRecentVideos(Pageable pageable) {
        Pageable sorted = PageRequest.of(
            pageable.getPageNumber(), pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "publishedAt")
        );
        return videoRepository.findByVisibility(VideoVisibility.PUBLIC, sorted)
                              .map(PublicVideoResponseDTO::fromEntity);
    }

    // ========================================================
    // ========== Ⅱ. 認証必須（Authenticated APIs） ===========
    // ========================================================

    /**
     * 動画を新規作成します（認証必須）。
     *
     * @param userId  作成者のユーザーID
     * @param request 動画作成リクエストDTO
     * @return 作成された動画のレスポンスDTO
     * @throws IllegalArgumentException タイトルまたは動画パスがnullの場合
     */
    @Transactional
    public VideoResponseDTO createVideo(UUID userId, VideoCreateRequestDTO request) {
        if (request == null || request.getTitle() == null || request.getVideoPath() == null) {
            throw new IllegalArgumentException("タイトルと動画パスは必須です");
        }
        User userRef = userRepository.getReferenceById(userId);
        Video video = new Video(
            request.getTitle(),
            request.getDescription(),
            request.getVideoPath(),
            request.getThumbnailPath(),
            userRef // 所有者
        );
        return VideoResponseDTO.fromEntity(videoRepository.save(video));
    }

    /**
     * 現在のユーザー（引数 userId）が所有する動画の一覧を取得します（認証必須）。
     *
     * @param userId   ユーザーID
     * @param pageable ページ情報
     * @return 動画レスポンスDTOのページ
     */
    public Page<VideoResponseDTO> getMyVideos(UUID userId, Pageable pageable) {
        return videoRepository.findByUserId(userId, pageable)
                              .map(VideoResponseDTO::fromEntity);
    }

    /**
     * 動画情報を更新します（認証前提。実オーナー判定は呼び出し元/セキュリティ層で実施）。
     *
     * @param id      動画ID
     * @param request 更新内容
     * @return 更新後のレスポンスDTO
     * @throws IllegalArgumentException リクエストがnullの場合
     */
    @Transactional
    public VideoResponseDTO updateVideo(UUID id, VideoUpdateRequestDTO request) {
        Video video = getActiveVideoOrThrow(id);
        if (request == null) {
            throw new IllegalArgumentException("更新内容が指定されていません");
        }
        video.updateVideoInfo(
            request.getTitle(),
            request.getDescription(),
            request.getThumbnailPath(),
            request.getVisibility(),
            request.getStatus()
        );
        return VideoResponseDTO.fromEntity(video);
    }

    /**
     * 動画を論理削除します（認証前提。実オーナー判定は呼び出し元/セキュリティ層で実施）。
     *
     * @param id 動画ID
     */
    @Transactional
    public void deleteVideo(UUID id) {
        Video video = getActiveVideoOrThrow(id);
        video.softDelete();
        videoRepository.save(video);
    }

    /**
     * 動画の再生数を1増加させます（認証は不要だが更新系のため設計方針により制御可）。
     *
     * @param id 動画ID
     */
    @Transactional
    public void incrementViews(UUID id) {
        Video video = getActiveVideoOrThrow(id);
        video.incrementViews();
    }

    /**
     * 動画を公開状態に設定します（認証前提。実オーナー判定は呼び出し元/セキュリティ層で実施）。
     *
     * @param id          動画ID
     * @param publishedAt 公開日時（null可）
     */
    @Transactional
    public void publishVideo(UUID id, LocalDateTime publishedAt) {
        Video video = getActiveVideoOrThrow(id);
        video.publish(publishedAt);
    }

    /**
     * 動画を非公開状態に戻します（認証前提。実オーナー判定は呼び出し元/セキュリティ層で実施）。
     *
     * @param id 動画ID
     */
    @Transactional
    public void unpublishVideo(UUID id) {
        Video video = getActiveVideoOrThrow(id);
        video.unpublish();
    }

    // ========================================================
    // ========== Ⅲ. 管理者権限必須（Admin-only APIs） ========
    // ========================================================

    /**
     * 論理削除された動画を復元します（管理者想定）。
     *
     * @param id 動画ID
     * @return 復元された動画のレスポンスDTO
     * @throws NoSuchElementException 動画が存在しない場合
     * @throws IllegalStateException  動画が削除状態でない場合
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
}
