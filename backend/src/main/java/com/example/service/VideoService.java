package com.example.service;

import com.example.dto.videos.*;
import com.example.entity.User;
import com.example.entity.Video;
import com.example.enums.VideoSort;
import com.example.enums.VideoVisibility;
import com.example.repository.UserRepository;
import com.example.repository.VideoRepository;
import com.example.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    /** 🔑 現在のユーザーを取得 */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("認証ユーザーが取得できません");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("ユーザー情報が存在しません"));
    }

    /** 動画新規作成 */
    @Transactional
    public VideoResponseDTO createVideo(VideoCreateRequestDTO request) {
        if (request.getTitle() == null || request.getVideoPath() == null) {
            throw new IllegalArgumentException("タイトルと動画パスは必須です");
        }

        User user = getCurrentUser();
        Video video = new Video(request.getTitle(), request.getDescription(), request.getVideoPath(),
                request.getThumbnailPath(), user);

        return VideoResponseDTO.fromEntity(videoRepository.save(video));
    }

    /** 動画取得 */
    public VideoResponseDTO getVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("動画が見つかりません (ID: " + id + ")"));
        return VideoResponseDTO.fromEntity(video);
    }

    /** 自分の動画一覧 */
    public Page<VideoResponseDTO> getMyVideos(Pageable pageable) {
        User user = getCurrentUser();
        Page<Video> videos = videoRepository.findByUserId(user.getId(), pageable);
        return videos.map(VideoResponseDTO::fromEntity);
    }

    /** 公開動画 検索 */
    public Page<PublicVideoResponseDTO> searchPublicVideos(SearchRequestDTO request, Pageable pageable) {
        String query = request.getQuery() != null ? request.getQuery() : "";

        Pageable effectivePageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            Sort sort = request.getSortBy() == VideoSort.VIEWS_COUNT
                    ? Sort.by(Sort.Direction.DESC, "viewsCount")
                    : Sort.by(Sort.Direction.DESC, "publishedAt");
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        Page<Video> result = videoRepository.findByTitleContainingIgnoreCaseAndVisibility(query, VideoVisibility.PUBLIC, effectivePageable);
        return result.map(PublicVideoResponseDTO::fromEntity);
    }

    /** 特定ユーザーの公開動画 */
    public Page<PublicVideoResponseDTO> getPublicVideosByUser(UUID userId, Pageable pageable) {
        Page<Video> videos = videoRepository.findByUserIdAndVisibility(userId, VideoVisibility.PUBLIC, pageable);
        return videos.map(PublicVideoResponseDTO::fromEntity);
    }

    /** 人気順 */
    public Page<PublicVideoResponseDTO> getPopularVideos(Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "viewsCount"));
        Page<Video> videos = videoRepository.findByVisibility(VideoVisibility.PUBLIC, sorted);
        return videos.map(PublicVideoResponseDTO::fromEntity);
    }

    /** 新着順 */
    public Page<PublicVideoResponseDTO> getRecentVideos(Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Video> videos = videoRepository.findByVisibility(VideoVisibility.PUBLIC, sorted);
        return videos.map(PublicVideoResponseDTO::fromEntity);
    }

    /** 動画更新 */
    @Transactional
    public VideoResponseDTO updateVideo(Long id, VideoUpdateRequestDTO request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("動画が見つかりません (ID: " + id + ")"));

        if (request == null) {
            throw new IllegalArgumentException("更新内容が指定されていません");
        }

        video.updateVideoInfo(request.getTitle(), request.getDescription(), request.getThumbnailPath(),
                request.getVisibility(), request.getStatus());

        return VideoResponseDTO.fromEntity(video);
    }

    /** 動画削除 */
    @Transactional
    public void deleteVideo(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new NoSuchElementException("動画が見つかりません (ID: " + id + ")");
        }
        videoRepository.deleteById(id);
    }
}
