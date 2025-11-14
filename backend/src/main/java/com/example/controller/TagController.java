package com.example.controller;

import com.example.dto.tags.*;
import com.example.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Tag API
 * 公開 → 認証必須 → 管理者必須 の順にセクション化
 */
@Tag(name = "Tags", description = "タグ関連API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;

    // ========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // ========================================================

    @Operation(summary = "タグ取得", description = "IDでタグを1件取得します（公開）")
    @GetMapping("/tags/{id}")
    public ResponseEntity<TagResponseDTO> getTag(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.getTag(id));
    }

    @Operation(summary = "タグ検索", description = "名称の部分一致でタグを検索します（公開）")
    @GetMapping("/tags")
    public ResponseEntity<Page<TagResponseDTO>> searchTags(
            @ParameterObject @Valid TagSearchRequestDTO request,
            @ParameterObject Pageable pageable) {
        Page<TagResponseDTO> page = tagService.searchTags(request, pageable);
        return ResponseEntity.ok(page);
    }

    // ========================================================
    // ============ Ⅱ. 認証必須（Authenticated） ==============
    // ========================================================

    @Operation(summary = "動画にタグを追加", description = "指定動画にタグを追加します（認証必須）")
    @PostMapping("/videos/{videoId}/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addTagsToVideo(@PathVariable UUID videoId,
            @RequestBody List<UUID> tagIds) {
        tagService.addTagsToVideo(videoId, tagIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "動画のタグを置換", description = "指定動画のタグを丸ごと置換します（認証必須）")
    @PutMapping("/videos/{videoId}/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> replaceVideoTags(@PathVariable UUID videoId,
            @RequestBody List<UUID> tagIds) {
        tagService.replaceVideoTag(videoId, tagIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "動画からタグを1つ外す", description = "指定動画から単一タグを解除します（認証必須）")
    @DeleteMapping("/videos/{videoId}/tags/{tagId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeTagFromVideo(@PathVariable UUID videoId,
            @PathVariable UUID tagId) {
        tagService.removeTagFromVideo(videoId, tagId);
        return ResponseEntity.noContent().build();
    }

    // ---------- LiveStream × Tag ----------

    @Operation(summary = "ライブ配信にタグを追加", description = "指定ライブ配信にタグを追加します（認証必須）")
    @PostMapping("/livestreams/{liveStreamId}/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addTagsToLiveStream(@PathVariable UUID liveStreamId,
            @RequestBody List<UUID> tagIds) {
        tagService.addTagsToLiveStream(liveStreamId, tagIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "ライブ配信のタグを置換", description = "指定ライブ配信のタグを丸ごと置換します（認証必須）")
    @PutMapping("/livestreams/{liveStreamId}/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> replaceLiveStreamTags(@PathVariable UUID liveStreamId,
            @RequestBody List<UUID> tagIds) {
        tagService.replaceLiveStreamTag(liveStreamId, tagIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "ライブ配信からタグを1つ外す", description = "指定ライブ配信から単一タグを解除します（認証必須）")
    @DeleteMapping("/livestreams/{liveStreamId}/tags/{tagId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeTagFromLiveStream(@PathVariable UUID liveStreamId,
            @PathVariable UUID tagId) {
        tagService.removeTagFromLiveStream(liveStreamId, tagId);
        return ResponseEntity.noContent().build();
    }

    // ========================================================
    // ============== Ⅲ. 管理者必須（Admin-only） =============
    // ========================================================

    @Operation(summary = "タグ作成", description = "新しいタグを作成します（管理者）")
    @PostMapping("/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponseDTO> createTag(
            @Valid @RequestBody TagCreateRequestDTO request) {
        TagResponseDTO created = tagService.createTag(request);
        return ResponseEntity.created(URI.create("/api/tags/" + created.getId()))
                .body(created);
    }

    @Operation(summary = "タグ更新", description = "タグ名・説明を更新します（管理者）")
    @PutMapping("/tags/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponseDTO> updateTag(@PathVariable UUID id,
            @Valid @RequestBody TagUpdateRequestDTO request) {
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    @Operation(summary = "タグ削除", description = "タグを削除します（管理者）")
    @DeleteMapping("/tags/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
