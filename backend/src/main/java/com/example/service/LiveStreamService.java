package com.example.service;

import com.example.dto.live_streams.*;
import com.example.entity.LiveStream;
import com.example.entity.User;
import com.example.enums.StreamStatus;
import com.example.repository.LiveStreamRepository;
import com.example.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LiveStreamService
 *
 * 配信機能に関連するビジネスロジックを提供するサービス層クラス。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveStreamService {

    private final LiveStreamRepository liveStreamRepository;
    private final UserRepository userRepository;

    // ========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // ========================================================

    /**
     * IDを指定してライブ配信を取得する。
     *
     * @param id ライブ配信ID
     * @return ライブ配信のレスポンスDTO
     * @throws NoSuchElementException 該当配信が見つからない、または削除されている場合
     */
    public LiveStreamResponseDTO getLiveStreamById(UUID id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * ステータスでフィルタされた配信一覧を取得する。
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByStatus(StreamStatus status, Pageable pageable) {
        return liveStreamRepository.findByStatus(status, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * 複数ステータスでフィルタされた配信一覧を取得する。
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByStatuses(List<StreamStatus> statuses, Pageable pageable) {
        return liveStreamRepository.findByStatusIn(statuses, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * タイトルとステータスで絞り込んだ配信一覧を取得する。
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByTitleAndStatus(
            String title, StreamStatus status, Pageable pageable) {
        return liveStreamRepository.findByTitleContainingIgnoreCaseAndStatus(title, status, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * streamKeyから配信を取得する。
     *
     * @throws NoSuchElementException 該当配信が存在しない場合
     */
    public LiveStream getLiveStreamByStreamKey(String streamKey) {
        return liveStreamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new NoSuchElementException("配信が見つかりません"));
    }

    // ========================================================
    // ============ Ⅱ. 認証必須（Authenticated） ==============
    // ========================================================

    /**
     * ライブ配信を新規作成する（本人）。
     *
     * @param userId  作成者のユーザーID
     * @param request 作成リクエストDTO
     * @return 作成されたライブ配信のレスポンスDTO
     */
    @Transactional
    public LiveStreamResponseDTO createLiveStream(UUID userId, LiveStreamCreateRequestDTO request) {
        User userRef = userRepository.getReferenceById(userId);
        LiveStream stream = new LiveStream(
                request.getTitle(),
                request.getDescription(),
                request.getThumbnailPath(),
                request.getScheduledAt(),
                userRef
        );
        return LiveStreamResponseDTO.fromEntity(liveStreamRepository.save(stream));
    }

    /**
     * 現在のユーザーのライブ配信をページング付きで取得する（本人）。
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByUser(UUID userId, Pageable pageable) {
        return liveStreamRepository.findByUserId(userId, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * ユーザーとステータスで絞り込んだライブ配信を取得する（本人）。
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByUserAndStatus(
            UUID userId, StreamStatus status, Pageable pageable) {
        return liveStreamRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * 配信情報（タイトル・説明・サムネイル）を更新する（本人）。
     */
    @Transactional
    public LiveStreamResponseDTO updateLiveStream(UUID id, LiveStreamUpdateRequestDTO request) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.updateLiveStreamInfo(request.getTitle(), request.getDescription(), request.getThumbnailPath());
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 配信予定日時を更新する（本人）。
     */
    @Transactional
    public LiveStreamResponseDTO rescheduleLiveStream(UUID id, LiveStreamReSchedul request) {
        LiveStream stream = getLiveStreamOrThrow(id);
        if (request.getScheduledAt() != null) {
            stream.reschedule(request.getScheduledAt());
        }
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 配信を論理削除する（本人／キャンセル扱い）。
     */
    @Transactional
    public void deleteLiveStream(UUID id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.cancel();
    }

    /**
     * 配信を開始状態に変更する（本人）。
     */
    @Transactional
    public LiveStreamResponseDTO openLiveStream(UUID id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.open();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 配信を終了状態に変更する（本人）。
     */
    @Transactional
    public LiveStreamResponseDTO closeLiveStream(UUID id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.close();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 配信をキャンセル状態に変更する（本人）。
     */
    @Transactional
    public LiveStreamResponseDTO cancelLiveStream(UUID id) {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.cancel();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    // ========================================================
    // ============== Ⅲ. 管理者必須（Admin-only） =============
    // ========================================================

    /**
     * 論理削除状態のライブ配信を復元する（管理者）。
     *
     * @throws NoSuchElementException 配信が見つからない場合
     * @throws IllegalStateException  未削除状態だった場合
     */
    @Transactional
    public LiveStreamResponseDTO restoreLiveStream(UUID id) {
        LiveStream stream = liveStreamRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new NoSuchElementException("配信が見つかりません"));
        if (!stream.isDeleted()) {
            throw new IllegalStateException("この配信は削除されていません");
        }
        stream.restore();
        return LiveStreamResponseDTO.fromEntity(liveStreamRepository.save(stream));
    }

    // ========================================================
    // =============== INTERNAL UTILITY METHOD ================
    // ========================================================

    /**
     * 削除されていないライブ配信を取得。
     *
     * @param id ライブ配信ID
     * @return ライブ配信エンティティ
     * @throws NoSuchElementException 該当IDの配信が存在しない、または削除済み
     */
    private LiveStream getLiveStreamOrThrow(UUID id) {
        return liveStreamRepository.findById(id)
                .filter(stream -> !stream.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("配信が見つからないか、削除されています"));
    }
}
