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

@Tag(name = "LiveStreams", description = "ライブ配信に関するAPI群") @RestController @RequestMapping("/api/live-streams")
@RequiredArgsConstructor
public class LiveStreamController {

    private final LiveStreamService liveStreamService;

    @Operation(summary = "ライブ配信作成", description = "新しいライブ配信を作成します。ストリームキーは自動生成され、作成には認証が必要です。")
    @SecurityRequirement(name = "bearerAuth") @PostMapping
    public ResponseEntity<LiveStreamResponseDTO> createLiveStream(@Valid @RequestBody
    LiveStreamCreateRequestDTO request,
            @Parameter(hidden = true, description = "現在認証されているユーザー") @RequestAttribute("currentUser")
            User currentUser) {
        return ResponseEntity.ok(liveStreamService.createLiveStream(request));
    }

    @Operation(summary = "ライブ配信詳細取得", description = "指定されたIDのライブ配信情報を取得します。公開・非公開問わず取得可能です。") @GetMapping("/{id}")
    public ResponseEntity<LiveStreamResponseDTO> getLiveStreamById(@Parameter(description = "ライブ配信ID") @PathVariable
    UUID id) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamById(id));
    }

    @Operation(summary = "ライブ配信情報更新", description = "既存のライブ配信のタイトル・説明・サムネイルを更新します。認証が必要です。")
    @SecurityRequirement(name = "bearerAuth") @PutMapping("/{id}")
    public ResponseEntity<LiveStreamResponseDTO> updateLiveStream(@Parameter(description = "ライブ配信ID") @PathVariable
    UUID id, @Valid @RequestBody
    LiveStreamUpdateRequestDTO request) {
        return ResponseEntity.ok(liveStreamService.updateLiveStream(id, request));
    }

    @Operation(summary = "ライブ配信スケジュール更新", description = "配信予定日時やステータスを更新します。主に配信の延期・変更に使用します。認証が必要です。")
    @SecurityRequirement(name = "bearerAuth") @PutMapping("/{id}/reschedule")
    public ResponseEntity<LiveStreamResponseDTO> rescheduleLiveStream(@Parameter(description = "ライブ配信ID") @PathVariable
    UUID id, @Valid @RequestBody
    LiveStreamReSchedul request) {
        return ResponseEntity.ok(liveStreamService.rescheduleLiveStream(id, request));
    }

    @Operation(summary = "ライブ配信削除", description = "指定されたライブ配信を削除します。認証が必要です。") @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiveStream(@Parameter(description = "ライブ配信ID") @PathVariable
    UUID id) {
        liveStreamService.deleteLiveStream(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "ユーザー別ライブ一覧取得", description = "指定ユーザーIDに紐づくライブ配信の一覧をページング付きで取得します。")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByUser(
            @Parameter(description = "ユーザーID") @PathVariable
            UUID userId, @Parameter(description = "ページング情報") @ParameterObject
            Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByUser(userId, pageable));
    }

    @Operation(summary = "ユーザー＆ステータス別ライブ一覧", description = "指定ユーザーIDかつ特定の配信ステータスで絞り込んだ配信一覧を返します。")
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByUserAndStatus(
            @Parameter(description = "ユーザーID") @PathVariable
            UUID userId, @Parameter(description = "配信ステータス") @RequestParam
            StreamStatus status, @Parameter(description = "ページング情報") @ParameterObject
            Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByUserAndStatus(userId, status, pageable));
    }

    @Operation(summary = "ステータス別ライブ一覧", description = "配信ステータスに基づく配信一覧をページング付きで取得します。") @GetMapping("/status")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByStatus(
            @Parameter(description = "配信ステータス") @RequestParam
            StreamStatus status, @Parameter(description = "ページング情報") @ParameterObject
            Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByStatus(status, pageable));
    }

    @Operation(summary = "複数ステータスのライブ一覧", description = "複数の配信ステータスでフィルタリングした配信一覧を取得します。") @GetMapping("/statuses")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByStatuses(
            @Parameter(description = "配信ステータスのリスト") @RequestParam
            List<StreamStatus> statuses, @Parameter(description = "ページング情報") @ParameterObject
            Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByStatuses(statuses, pageable));
    }

    @Operation(summary = "タイトル検索付きライブ一覧", description = "タイトル部分一致とステータス指定で配信を検索します。") @GetMapping("/search")
    public ResponseEntity<Page<LiveStreamResponseDTO>> getLiveStreamsByTitleAndStatus(
            @Parameter(description = "タイトルキーワード") @RequestParam
            String title, @Parameter(description = "配信ステータス") @RequestParam
            StreamStatus status, @Parameter(description = "ページング情報") @ParameterObject
            Pageable pageable) {
        return ResponseEntity.ok(liveStreamService.getLiveStreamsByTitleAndStatus(title, status, pageable));
    }

    @Operation(summary = "StreamKeyから配信取得", description = "配信用のStream Keyを指定してライブ配信情報を取得します。")
    @GetMapping("/key/{streamKey}")
    public ResponseEntity<LiveStreamResponseDTO> getLiveStreamByStreamKey(
            @Parameter(description = "ストリームキー") @PathVariable
            String streamKey) {
        return ResponseEntity
                .ok(LiveStreamResponseDTO.fromEntity(liveStreamService.getLiveStreamByStreamKey(streamKey)));
    }

    @Operation(summary = "ライブ開始", description = "指定配信IDのステータスをLIVEに変更します。認証が必要です。")
    @SecurityRequirement(name = "bearerAuth") @PostMapping("/{id}/open")
    public ResponseEntity<LiveStreamResponseDTO> openLiveStream(@Parameter(description = "ライブ配信ID") @PathVariable
    UUID id) {
        return ResponseEntity.ok(liveStreamService.openLiveStream(id));
    }

    @Operation(summary = "ライブ終了", description = "指定配信IDのステータスを終了状態に変更します。認証が必要です。")
    @SecurityRequirement(name = "bearerAuth") @PostMapping("/{id}/close")
    public ResponseEntity<LiveStreamResponseDTO> closeLiveStream(@Parameter(description = "ライブ配信ID") @PathVariable
    UUID id) {
        return ResponseEntity.ok(liveStreamService.closeLiveStream(id));
    }

    @Operation(summary = "ライブキャンセル", description = "指定配信IDのステータスをキャンセル状態に変更します。認証が必要です。")
    @SecurityRequirement(name = "bearerAuth") @PostMapping("/{id}/cancel")
    public ResponseEntity<LiveStreamResponseDTO> cancelLiveStream(@Parameter(description = "ライブ配信ID") @PathVariable
    UUID id) {
        return ResponseEntity.ok(liveStreamService.cancelLiveStream(id));
    }
}
