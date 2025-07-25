package com.example.service;

import com.example.dto.live_streams.*;
import com.example.entity.LiveStream;
import com.example.entity.User;
import com.example.enums.StreamStatus;
import com.example.repository.LiveStreamRepository;
import com.example.util.CurrentUserUtil;
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
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class LiveStreamService {

    private final LiveStreamRepository liveStreamRepository;
    private final CurrentUserUtil currentUserUtil;

    // =============================
    // ========== CREATE ==========
    // =============================

    /**
     * ライブ配信を新規作成する。
     *
     * @param request
     *            作成リクエストDTO
     * @return 作成されたライブ配信のレスポンスDTO
     * @throws IllegalStateException
     *             認証ユーザーが取得できない場合
     */
    @Transactional
    public LiveStreamResponseDTO createLiveStream(LiveStreamCreateRequestDTO request)
    {
        User user = currentUserUtil.getCurrentUser();
        LiveStream stream = new LiveStream(request.getTitle(), request.getDescription(), request.getThumbnailPath(),
                request.getScheduledAt(), user);
        return LiveStreamResponseDTO.fromEntity(liveStreamRepository.save(stream));
    }

    // ============================
    // ========== READ ===========
    // ============================

    /**
     * IDを指定してライブ配信を取得する。
     *
     * @param id
     *            ライブ配信ID
     * @return ライブ配信のレスポンスDTO
     * @throws NoSuchElementException
     *             該当配信が見つからない、または削除されている場合
     */
    public LiveStreamResponseDTO getLiveStreamById(UUID id)
    {
        LiveStream stream = getLiveStreamOrThrow(id);
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 現在のユーザーのライブ配信をページング付きで取得する。
     *
     * @param userId
     *            ユーザーID
     * @param pageable
     *            ページング情報
     * @return ライブ配信レスポンスDTOのページ
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByUser(UUID userId, Pageable pageable)
    {
        return liveStreamRepository.findByUserId(userId, pageable).map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * ユーザーとステータスで絞り込んだライブ配信を取得する。
     *
     * @param userId
     *            ユーザーID
     * @param status
     *            配信ステータス
     * @param pageable
     *            ページング情報
     * @return フィルタ済みライブ配信レスポンスDTOのページ
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByUserAndStatus(UUID userId, StreamStatus status,
            Pageable pageable)
    {
        return liveStreamRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * ステータスでフィルタされた配信一覧を取得する。
     *
     * @param status
     *            配信ステータス
     * @param pageable
     *            ページング情報
     * @return 配信一覧ページ
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByStatus(StreamStatus status, Pageable pageable)
    {
        return liveStreamRepository.findByStatus(status, pageable).map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * 複数ステータスでフィルタされた配信一覧を取得する。
     *
     * @param statuses
     *            配信ステータス一覧
     * @param pageable
     *            ページング情報
     * @return 配信一覧ページ
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByStatuses(List<StreamStatus> statuses, Pageable pageable)
    {
        return liveStreamRepository.findByStatusIn(statuses, pageable).map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * タイトルとステータスで絞り込んだ配信一覧を取得する。
     *
     * @param title
     *            タイトル部分一致
     * @param status
     *            ステータス
     * @param pageable
     *            ページング
     * @return 絞り込んだ配信一覧ページ
     */
    public Page<LiveStreamResponseDTO> getLiveStreamsByTitleAndStatus(String title, StreamStatus status,
            Pageable pageable)
    {
        return liveStreamRepository.findByTitleContainingIgnoreCaseAndStatus(title, status, pageable)
                .map(LiveStreamResponseDTO::fromEntity);
    }

    /**
     * streamKeyから配信を取得する。
     *
     * @param streamKey
     *            配信用キー
     * @return ライブ配信エンティティ
     * @throws NoSuchElementException
     *             該当配信が存在しない場合
     */
    public LiveStream getLiveStreamByStreamKey(String streamKey)
    {
        return liveStreamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new NoSuchElementException("配信が見つかりません"));
    }

    // =============================
    // ========== UPDATE ==========
    // =============================

    /**
     * 配信情報（タイトル・説明・サムネイル）を更新する。
     *
     * @param id
     *            配信ID
     * @param request
     *            更新リクエストDTO
     * @return 更新後のレスポンスDTO
     * @throws NoSuchElementException
     *             配信が存在しない、または削除済み
     */
    @Transactional
    public LiveStreamResponseDTO updateLiveStream(UUID id, LiveStreamUpdateRequestDTO request)
    {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.updateLiveStreamInfo(request.getTitle(), request.getDescription(), request.getThumbnailPath());
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 配信予定日時を更新する。
     *
     * @param id
     *            配信ID
     * @param request
     *            予約変更リクエストDTO
     * @return 更新後の配信レスポンスDTO
     */
    @Transactional
    public LiveStreamResponseDTO rescheduleLiveStream(UUID id, LiveStreamReSchedul request)
    {
        LiveStream stream = getLiveStreamOrThrow(id);
        if (request.getScheduledAt() != null)
        {
            stream.reschedule(request.getScheduledAt());
        }
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    // =============================
    // ========== DELETE ==========
    // =============================

    /**
     * 配信を論理削除する（キャンセル扱い）。
     *
     * @param id
     *            配信ID
     * @throws NoSuchElementException
     *             配信が存在しない、または既に削除済み
     */
    @Transactional
    public void deleteLiveStream(UUID id)
    {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.cancel();
    }

    /**
     * 論理削除状態のライブ配信を復元する。
     *
     * @param id
     *            配信ID
     * @return 復元後のレスポンスDTO
     * @throws NoSuchElementException
     *             配信が見つからない場合
     * @throws IllegalStateException
     *             未削除状態だった場合
     */
    @Transactional
    public LiveStreamResponseDTO restoreLiveStream(UUID id)
    {
        LiveStream stream = liveStreamRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new NoSuchElementException("配信が見つかりません"));
        if (!stream.isDeleted())
        {
            throw new IllegalStateException("この配信は削除されていません");
        }
        stream.restore();
        return LiveStreamResponseDTO.fromEntity(liveStreamRepository.save(stream));
    }

    // =============================
    // ========== STATUS ===========
    // =============================

    /**
     * 配信を開始状態に変更する。
     *
     * @param id
     *            配信ID
     * @return 更新後のレスポンスDTO
     */
    @Transactional
    public LiveStreamResponseDTO openLiveStream(UUID id)
    {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.open();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 配信を終了状態に変更する。
     *
     * @param id
     *            配信ID
     * @return 更新後のレスポンスDTO
     */
    @Transactional
    public LiveStreamResponseDTO closeLiveStream(UUID id)
    {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.close();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    /**
     * 配信をキャンセル状態に変更する。
     *
     * @param id
     *            配信ID
     * @return 更新後のレスポンスDTO
     */
    @Transactional
    public LiveStreamResponseDTO cancelLiveStream(UUID id)
    {
        LiveStream stream = getLiveStreamOrThrow(id);
        stream.cancel();
        return LiveStreamResponseDTO.fromEntity(stream);
    }

    // =============================================
    // ========== INTERNAL UTILITY METHOD ==========
    // =============================================

    /**
     * 削除されていないライブ配信を取得。
     *
     * @param id
     *            ライブ配信ID
     * @return ライブ配信エンティティ
     * @throws NoSuchElementException
     *             該当IDの配信が存在しない、または削除済み
     */
    private LiveStream getLiveStreamOrThrow(UUID id)
    {
        return liveStreamRepository.findById(id).filter(stream -> !stream.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("配信が見つからないか、削除されています"));
    }
}
