package com.example.service;

import com.example.dto.videos.PublicVideoResponseDTO;
import com.example.dto.videos.SearchRequestDTO;
import com.example.dto.videos.VideoCreateRequestDTO;
import com.example.dto.videos.VideoUpdateRequestDTO;
import com.example.dto.videos.VideoResponseDTO;
import com.example.entity.User;
import com.example.entity.Video;
import com.example.enums.VideoSort;
import com.example.enums.VideoVisibility;
import com.example.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoService {

    private final VideoRepository videoRepository;

    /**
     * 動画新規作成
     */
    @Transactional
    public VideoResponseDTO createVideo(VideoCreateRequestDTO request, User user) {
        if (request.getTitle() == null || request.getVideoPath() == null) {
            throw new IllegalArgumentException("タイトルと動画パスは必須です");
        }

        Video video = new Video(
            request.getTitle(),
            request.getDescription(),
            request.getVideoPath(),
            request.getThumbnailPath(),
            user
        );

        return VideoResponseDTO.fromEntity(videoRepository.save(video));
    }

    /**
     * 動画取得
     */
    public VideoResponseDTO getVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("動画が見つかりません (ID: " + id + ")"));
        return VideoResponseDTO.fromEntity(video);
    }

    public Page<VideoResponseDTO> getMyVideos(UUID userId, Pageable pageable) {
        Page<Video> videos = videoRepository.findByUserId(userId, pageable);
        return videos.map(VideoResponseDTO::fromEntity);
    }

    public Page<PublicVideoResponseDTO> searchPublicVideos(SearchRequestDTO request, Pageable pageable) {
        String query = request.getQuery() != null ? request.getQuery() : "";

        // ソート指定がなければデフォルトをセット
        Pageable effectivePageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            if (request.getSortBy() == VideoSort.VIEWS_COUNT) {
                effectivePageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "viewsCount")
                );
            } else {
                effectivePageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "publishedAt")
                );
            }
        }

        Page<Video> result = videoRepository.findByTitleContainingIgnoreCaseAndVisibility(query, VideoVisibility.PUBLIC, effectivePageable);

        return result.map(PublicVideoResponseDTO::fromEntity);
    }

    public Page<PublicVideoResponseDTO> getPublicVideosByUser(UUID userId, Pageable pageable) {
        Page<Video> videos = videoRepository.findByUserIdAndVisibility(userId, VideoVisibility.PUBLIC, pageable);
        return videos.map(PublicVideoResponseDTO::fromEntity);
    }

    public Page<PublicVideoResponseDTO> getPopularVideos(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "viewsCount")
        );

        Page<Video> videos = videoRepository.findByVisibility(VideoVisibility.PUBLIC, sortedPageable);
        return videos.map(PublicVideoResponseDTO::fromEntity);
    }

    public Page<PublicVideoResponseDTO> getRecentVideos(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "publishedAt")
        );
        Page<Video> videos = videoRepository.findByVisibility(VideoVisibility.PUBLIC, sortedPageable);
        return videos.map(PublicVideoResponseDTO::fromEntity);
    }






    /**
     * 動画更新
     */
    @Transactional
    public VideoResponseDTO updateVideo(Long id, VideoUpdateRequestDTO request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("動画が見つかりません (ID: " + id + ")"));

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
     * 動画削除
     */
    @Transactional
    public void deleteVideo(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new NoSuchElementException("動画が見つかりません (ID: " + id + ")");
        }
        videoRepository.deleteById(id);
    }
}
