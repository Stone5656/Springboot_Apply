package com.example.controller;

import com.example.dto.videos.PublicVideoResponseDTO;
import com.example.dto.videos.SearchRequestDTO;
import com.example.dto.videos.VideoCreateRequestDTO;
import com.example.dto.videos.VideoResponseDTO;
import com.example.dto.videos.VideoUpdateRequestDTO;
import com.example.entity.User;
import com.example.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Videos", description = "アーカイブ関連API")
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // ← 既定は認証必須。匿名許可は yml の例外で管理
public class VideoController {

    // ==========================================
    // # VideoController
    // ## 1. 公開API (Anonymous OK)   … yml の anonymous / permitAll 例外で通過
    // ## 2. 認証API (JWT必須)        … クラス直付けの SecurityRequirement に従う
    // ## 3. 管理者専用API             … yml 側でロール制御 (HAS_ROLE) を付与
    // ## 4. 入出力DTO・Validation     … DTO バリデーションは各 DTO 側で付与想定
    // ## 5. 例外ハンドリング方針      … ControllerAdvice で一元化（ドメイン例外→HTTP変換）
    // ==========================================

    private final VideoService videoService;

    // ------------------------------------------------
    // 1) 公開API（Anonymous OK）※ yml で例外指定する想定
    // ------------------------------------------------

    @Operation(summary = "idで特定の動画を検索", description = "公開/非公開いずれも取得ポリシーはサービス層で判定")
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDTO> getVideo(
            @Parameter(description = "動画ID") @PathVariable UUID id) {
        return ResponseEntity.ok(videoService.getVideo(id));
    }

    @Operation(summary = "公開動画を検索",
               description = "タイトル部分一致・viewsCount/publishedAt ソート・ページング対応")
    @GetMapping("/search")
    public ResponseEntity<Page<PublicVideoResponseDTO>> searchPublicVideos(
            @Parameter(description = "検索条件") @ModelAttribute SearchRequestDTO searchRequest,
            @Parameter(description = "ページング情報") Pageable pageable) {
        return ResponseEntity.ok(videoService.searchPublicVideos(searchRequest, pageable));
    }

    @Operation(summary = "特定ユーザーの公開動画一覧",
               description = "ユーザーIDの公開動画をページング取得")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PublicVideoResponseDTO>> getPublicVideosByUser(
            @Parameter(description = "ユーザーID") @PathVariable UUID userId,
            @Parameter(description = "ページング情報") Pageable pageable) {
        return ResponseEntity.ok(videoService.getPublicVideosByUser(userId, pageable));
    }

    @Operation(summary = "人気動画一覧", description = "再生回数順の公開動画一覧")
    @GetMapping("/popular")
    public ResponseEntity<Page<PublicVideoResponseDTO>> getPopularVideos(
            @Parameter(description = "ページング情報") Pageable pageable) {
        return ResponseEntity.ok(videoService.getPopularVideos(pageable));
    }

    @Operation(summary = "新着動画一覧", description = "公開日時の新しい順")
    @GetMapping("/recent")
    public ResponseEntity<Page<PublicVideoResponseDTO>> getRecentVideos(
            @Parameter(description = "ページング情報") Pageable pageable) {
        return ResponseEntity.ok(videoService.getRecentVideos(pageable));
    }

    @Operation(summary = "動画の再生数をカウントアップ", description = "再生イベント用（公開エンドポイント想定）")
    @PatchMapping("/{id}/views")
    public ResponseEntity<Void> incrementViews(@PathVariable UUID id) {
        videoService.incrementViews(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------
    // 2) 認証API（JWT必須）
    // ------------------------------------------------

    @Operation(summary = "自分の動画一覧を取得", description = "マイアーカイブ一覧API（要ログイン）")
    @GetMapping("/my")
    public ResponseEntity<Page<VideoResponseDTO>> getMyVideos(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "ページング情報") Pageable pageable) {
        return ResponseEntity.ok(videoService.getMyVideos(pageable));
    }

    @Operation(summary = "動画をアップロード", description = "Create 用（要ログイン）")
    @PostMapping("/upload")
    public ResponseEntity<VideoResponseDTO> createVideo(
            @Parameter(description = "新規動画のリクエストDTO") @RequestBody VideoCreateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(videoService.createVideo(request));
    }

    @Operation(summary = "idで特定の動画を更新", description = "Update 用（要ログイン）")
    @PutMapping("/{id}")
    public ResponseEntity<VideoResponseDTO> updateVideo(
            @Parameter(description = "動画ID") @PathVariable UUID id,
            @Parameter(description = "動画更新のリクエストDTO") @RequestBody VideoUpdateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(videoService.updateVideo(id, request));
    }

    @Operation(summary = "idで特定の動画を削除", description = "Delete 用（要ログイン）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(
            @Parameter(description = "動画ID") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "動画を公開状態にする", description = "公開日時を指定し公開（要ログイン）")
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publishVideo(
            @PathVariable UUID id,
            @RequestParam(required = false) String publishedAt // ISO-8601
    ) {
        LocalDateTime dateTime = (publishedAt != null) ? LocalDateTime.parse(publishedAt) : null;
        videoService.publishVideo(id, dateTime);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "動画を非公開にする", description = "公開済みを非公開へ戻す（要ログイン）")
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishVideo(@PathVariable UUID id) {
        videoService.unpublishVideo(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------
    // 3) 管理者専用API（例：論理削除の復元）※ yml で HAS_ROLE を付与
    // ------------------------------------------------

    @Operation(summary = "削除済み動画の復元", description = "論理削除からの復元（管理者想定）")
    @PostMapping("/{id}/restore")
    public ResponseEntity<VideoResponseDTO> restoreVideo(@PathVariable UUID id) {
        return ResponseEntity.ok(videoService.restoreVideo(id));
    }
}
