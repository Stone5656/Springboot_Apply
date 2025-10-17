package com.example.controller;

import com.example.dto.live_streams.*;
import com.example.entity.User;
import com.example.enums.StreamStatus;
import com.example.service.LiveStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "LiveStreams", description = "ライブ配信に関するAPI群")
@RestController
@RequestMapping("/api/live-streams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // 既定は認証必須。匿名許可は yml 側の例外で制御
public class LiveStreamController {
    // ==========================================
    // # LiveStreamController
    // ## 1. 公開API (Anonymous OK)
    // ## 2. 認証API (JWT必須)
    // ## 3. 管理者専用API (必要なら)
    // ## 4. 入出力DTO・バリデーション
    // ## 5. 例外ハンドリング方針
    // ==========================================

    private final LiveStreamService liveStreamService;

    // ------------------------------------------------
    // 1) 公開API（Anonymous OK）※ yml の anonymous / permitAll で例外指定
    // ------------------------------------------------

    @Operation(summary = "ライブ配信詳細取得", description = "指定IDのライブ配信情報を取得（公開/非公開の公開可否はサービス層で判定）")
    @GetMapping("/{id}")
    public ResponseEntity<LiveStreamResponseDTO> getLiveStreamById(
            @Parameter(description = "ライブ配信ID") @PathVariable UUID id) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamById(id));
    }

    @Operation(summary = "ユーザー別ライブ一覧取得", description = "指定ユーザーIDに紐づくライブ配信一覧（ページング）")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByUser(
            @Parameter(description = "ユーザーID") @PathVariable UUID userId,
            @Parameter(description = "ページング情報") @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByUser(userId, pageable));
    }

    @Operation(summary = "ユーザー＆ステータス別ライブ一覧", description = "ユーザーID×配信ステータスで絞り込み（ページング）")
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByUserAndStatus(
            @Parameter(description = "ユーザーID") @PathVariable UUID userId,
            @Parameter(description = "配信ステータス") @RequestParam StreamStatus status,
            @Parameter(description = "ページング情報") @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByUserAndStatus(userId, status, pageable));
    }

    @Operation(summary = "ステータス別ライブ一覧", description = "配信ステータスに基づく配信一覧（ページング）")
    @GetMapping("/status")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByStatus(
            @Parameter(description = "配信ステータス") @RequestParam StreamStatus status,
            @Parameter(description = "ページング情報") @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByStatus(status, pageable));
    }

    @Operation(summary = "複数ステータスのライブ一覧", description = "複数ステータスでフィルタリング（ページング）")
    @GetMapping("/statuses")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByStatuses(
            @Parameter(description = "配信ステータスのリスト") @RequestParam List<StreamStatus> statuses,
            @Parameter(description = "ページング情報") @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByStatuses(statuses, pageable));
    }

    @Operation(summary = "タイトル検索付きライブ一覧", description = "タイトル部分一致＋ステータス指定（ページング）")
    @GetMapping("/search")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByTitleAndStatus(
            @Parameter(description = "タイトルキーワード") @RequestParam String title,
            @Parameter(description = "配信ステータス") @RequestParam StreamStatus status,
            @Parameter(description = "ページング情報") @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByTitleAndStatus(title, status, pageable));
    }

    @Operation(summary = "StreamKeyから配信取得", description = "配信用の Stream Key を指定して取得")
    @GetMapping("/key/{streamKey}")
    public ResponseEntity<LiveStreamResponseDTO> getLiveStreamByStreamKey(
            @Parameter(description = "ストリームキー") @PathVariable String streamKey) {
        return ResponseEntity.ok(
                LiveStreamResponseDTO.fromEntity(liveStreamService.getLiveStreamByStreamKey(streamKey)));
    }

    // ------------------------------------------------
    // 2) 認証API（JWT必須）
    // ------------------------------------------------

    @Operation(summary = "ライブ配信作成",
               description = "新規ライブ配信を作成。ストリームキーは自動生成（要ログイン）")
    @PostMapping
    public ResponseEntity<LiveStreamResponseDTO> createLiveStream(
            @Valid @RequestBody LiveStreamCreateRequestDTO request,
            @Parameter(hidden = true, description = "現在認証されているユーザー")
            @RequestAttribute("currentUser") User currentUser) {
        return ResponseEntity.ok(liveStreamService.createLiveStream(request));
    }

    @Operation(summary = "ライブ配信情報更新", description = "タイトル・説明・サムネイル等を更新（要ログイン）")
    @PutMapping("/{id}")
    public ResponseEntity<LiveStreamResponseDTO> updateLiveStream(
            @Parameter(description = "ライブ配信ID") @PathVariable UUID id,
            @Valid @RequestBody LiveStreamUpdateRequestDTO request) {
        return ResponseEntity.ok(liveStreamService.updateLiveStream(id, request));
    }

    @Operation(summary = "ライブ配信スケジュール更新",
               description = "予定日時やステータスを更新（延期/変更に使用・要ログイン）")
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<LiveStreamResponseDTO> rescheduleLiveStream(
            @Parameter(description = "ライブ配信ID") @PathVariable UUID id,
            @Valid @RequestBody LiveStreamReSchedul request) {
        return ResponseEntity.ok(liveStreamService.rescheduleLiveStream(id, request));
    }

    @Operation(summary = "ライブ開始", description = "配信IDのステータスを LIVE に変更（要ログイン）")
    @PostMapping("/{id}/open")
    public ResponseEntity<LiveStreamResponseDTO> openLiveStream(
            @Parameter(description = "ライブ配信ID") @PathVariable UUID id) {
        return ResponseEntity.ok(liveStreamService.openLiveStream(id));
    }

    @Operation(summary = "ライブ終了", description = "ステータスを 終了 に変更（要ログイン）")
    @PostMapping("/{id}/close")
    public ResponseEntity<LiveStreamResponseDTO> closeLiveStream(
            @Parameter(description = "ライブ配信ID") @PathVariable UUID id) {
        return ResponseEntity.ok(liveStreamService.closeLiveStream(id));
    }

    @Operation(summary = "ライブキャンセル", description = "ステータスを キャンセル に変更（要ログイン）")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<LiveStreamResponseDTO> cancelLiveStream(
            @Parameter(description = "ライブ配信ID") @PathVariable UUID id) {
        return ResponseEntity.ok(liveStreamService.cancelLiveStream(id));
    }

    @Operation(summary = "ライブ配信削除", description = "指定配信の削除（要ログイン）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiveStream(
            @Parameter(description = "ライブ配信ID") @PathVariable UUID id) {
        liveStreamService.deleteLiveStream(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------
    // 3) 管理者専用API（必要に応じて）※ yml で HAS_ROLE を付与
    // ------------------------------------------------
    // （現状なし：もし「強制終了」「復元」などを管理権限に限定するならここへ集約）
}
