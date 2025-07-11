package com.example.service;

import com.example.dto.live_stream.LiveStreamCreateRequestDTO;
import com.example.dto.live_stream.LiveStreamReSchedul;
import com.example.dto.live_stream.LiveStreamResponseDTO;
import com.example.dto.live_stream.LiveStreamUpdateRequestDTO;
import com.example.entity.LiveStream;
import com.example.entity.User;
import com.example.enums.StreamStatus;
import com.example.repository.LiveStreamRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveStreamService {

    private final LiveStreamRepository liveStreamRepository;

    /**
     * Create
     */
    @Transactional
    public LiveStreamResponseDTO createLiveStream(LiveStreamCreateRequestDTO request, User user) {
        LiveStream stream = new LiveStream(request.getTitle(), request.getDescription(), request.getThumbnailPath(),
                request.getScheduledAt(), user);
        stream.generateStreamKey();
        return LiveStreamResponseDTO.fromEntity(liveStreamRepository.save(stream));
    }

    /**
     * Read by ID
     */
    public LiveStreamResponseDTO getLiveStreamById(Long id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * Update (タイトル・説明・サムネ)
     */
    @Transactional
    public LiveStreamResponseDTO updateLiveStream(Long id, LiveStreamUpdateRequestDTO request) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.updateLiveStreamInfo(request.getTitle(), request.getDescription(), request.getThumbnailPath());
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * ReSchedule (Status + ScheduledAt)
     */
    @Transactional
    public LiveStreamResponseDTO rescheduleLiveStream(Long id, LiveStreamReSchedul request) {
        LiveStream stream = getLiveStreamOrThrow(id);

        if (request.getScheduledAt() != null) {
            stream.reschedule(request.getScheduledAt());
        }

        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * Delete
     */
    @Transactional
    public void deleteLiveStream(Long id) {
        if (!liveStreamRepository.existsById(id)) {
            throw new NoSuchElementException("配信が見つかりません");
        }
        liveStreamRepository.deleteById(id);
    }

    /**
     * Get by User
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByUser(UUID userId, Pageable pageable) {
        return liveStreamRepository.findByUserId(userId, pageable).map(LiveStreamResponseDTO::fromEntity);
    }

    public Page<LiveStreamResponseDTO> getLiveStreamsByUserAndStatus(UUID userId, StreamStatus status,
            Pageable pageable) {
        return liveStreamRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    public Page<LiveStreamResponseDTO> getLiveStreamsByStatus(StreamStatus status, Pageable pageable) {
        return liveStreamRepository.findByStatus(status, pageable).map(LiveStreamResponseDTO::fromEntity);
    }

    public Page<LiveStreamResponseDTO> getLiveStreamsByStatuses(List<StreamStatus> statuses, Pageable pageable) {
        return liveStreamRepository.findByStatusIn(statuses, pageable).map(LiveStreamResponseDTO::fromEntity);
    }

    public Page<LiveStreamResponseDTO> getLiveStreamsByTitleAndStatus(String title, StreamStatus status,
            Pageable pageable) {
        return liveStreamRepository.findByTitleContainingIgnoreCaseAndStatus(title, status, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    public LiveStream getLiveStreamByStreamKey(String streamKey) {
        return liveStreamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new NoSuchElementException("配信が見つかりません"));
    }

    /**
     * 状態変更
     */
    @Transactional
    public LiveStreamResponseDTO openLiveStream(Long id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.open();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    @Transactional
    public LiveStreamResponseDTO closeLiveStream(Long id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.close();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    @Transactional
    public LiveStreamResponseDTO cancelLiveStream(Long id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.cancel();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 共通：存在チェック取得
     */
    private LiveStream getLiveStreamOrThrow(Long id) {
        return liveStreamRepository.findById(id).orElseThrow(() -> new NoSuchElementException("配信が見つかりません"));
    }
}
