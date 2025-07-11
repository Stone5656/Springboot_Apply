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
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Videos", description = "アーカイブ関連API")
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "idで特定の動画を検索", description = "Read用API")
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDTO> getVideo(@Parameter(description = "動画ID") @PathVariable Long id) {
        VideoResponseDTO video = videoService.getVideo(id);
        return ResponseEntity.ok(video);
    }

    @Operation(summary = "自分の動画一覧を取得", description = "マイアーカイブ一覧API")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    public ResponseEntity<Page<VideoResponseDTO>> getMyVideos(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "ページング情報") Pageable pageable) {
        Page<VideoResponseDTO> myVideos = videoService.getMyVideos(pageable);
        return ResponseEntity.ok(myVideos);
    }

    @Operation(summary = "公開動画を検索", description = "タイトルによる部分一致、viewsCountまたはpublishedAtでソート、ページング対応")
    @GetMapping
    public ResponseEntity<Page<PublicVideoResponseDTO>> searchPublicVideos(
            @Parameter(description = "検索条件") @ModelAttribute SearchRequestDTO searchRequest,
            @Parameter(description = "ページング情報") Pageable pageable) {
        Page<PublicVideoResponseDTO> videos = videoService.searchPublicVideos(searchRequest, pageable);
        return ResponseEntity.ok(videos);
    }

    @Operation(summary = "特定ユーザーの公開動画一覧", description = "ユーザーIDからその人の公開動画をページング付きで取得")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PublicVideoResponseDTO>> getPublicVideosByUser(
            @Parameter(description = "ユーザーID") @PathVariable UUID userId,
            @Parameter(description = "ページング情報") Pageable pageable) {
        Page<PublicVideoResponseDTO> videos = videoService.getPublicVideosByUser(userId,pageable);
        return ResponseEntity.ok(videos);
    }

    @Operation(summary = "人気動画一覧", description = "再生回数順の公開動画一覧")
    @GetMapping("/popular")
    public ResponseEntity<Page<PublicVideoResponseDTO>> getPopularVideos(
            @Parameter(description = "ページング情報") Pageable pageable) {
        return ResponseEntity.ok(videoService.getPopularVideos(pageable));
    }

    @Operation(summary = "新着動画一覧", description = "公開日時の新しい順の動画一覧")
    @GetMapping("/recent")
    public ResponseEntity<Page<PublicVideoResponseDTO>> getRecentVideos(
            @Parameter(description = "ページング情報") Pageable pageable) {
        return ResponseEntity.ok(videoService.getRecentVideos(pageable));
    }

    @Operation(summary = "動画をアップロード", description = "Create用API")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<VideoResponseDTO> createVideo(
            @Parameter(description = "新規動画のリクエストDTO") @RequestBody VideoCreateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        VideoResponseDTO created = videoService.createVideo(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "idで特定の動画を更新", description = "Update用API")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<VideoResponseDTO> updateVideo(@Parameter(description = "動画ID") @PathVariable Long id,
            @Parameter(description = "動画更新のリクエストDTO") @RequestBody VideoUpdateRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        VideoResponseDTO updated = videoService.updateVideo(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "idで特定の動画を削除", description = "Delete用API")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@Parameter(description = "動画ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}
