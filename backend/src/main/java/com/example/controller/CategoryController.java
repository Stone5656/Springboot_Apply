package com.example.controller;

import com.example.dto.categories.*;
import com.example.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Category API
 *
 * 公開 → 認証必須 → 管理者必須 の順にセクション化
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    // ========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // ========================================================

    @Operation(summary = "カテゴリ取得", description = "IDでカテゴリを1件取得します（公開）")
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @Operation(summary = "カテゴリ検索", description = "名称の部分一致でカテゴリを検索します（公開）")
    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryResponseDTO>> searchCategories(
            @ParameterObject @Valid CategorySearchRequestDTO request,
            @ParameterObject Pageable pageable
    ) {
        Page<CategoryResponseDTO> page = categoryService.searchCategories(request, pageable);
        return ResponseEntity.ok(page);
    }

    // ========================================================
    // ============ Ⅱ. 認証必須（Authenticated） ==============
    // ========================================================

    // ---------- Video × Category ----------

    @Operation(summary = "動画にカテゴリを追加", description = "指定動画にカテゴリを追加します（認証必須）")
    @PostMapping("/videos/{videoId}/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addCategoriesToVideo(
            @PathVariable UUID videoId,
            @RequestBody List<UUID> categoryIds // シンプルな配列ボディを受け付ける
    ) {
        categoryService.addCategoriesToVideo(videoId, categoryIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "動画のカテゴリを置換", description = "指定動画のカテゴリを丸ごと置換します（認証必須）")
    @PutMapping("/videos/{videoId}/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> replaceVideoCategories(
            @PathVariable UUID videoId,
            @RequestBody List<UUID> categoryIds
    ) {
        categoryService.replaceVideoCategories(videoId, categoryIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "動画からカテゴリを1つ外す", description = "指定動画から単一カテゴリを解除します（認証必須）")
    @DeleteMapping("/videos/{videoId}/categories/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeCategoryFromVideo(
            @PathVariable UUID videoId,
            @PathVariable UUID categoryId
    ) {
        categoryService.removeCategoryFromVideo(videoId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // ---------- LiveStream × Category ----------

    @Operation(summary = "ライブ配信にカテゴリを追加", description = "指定ライブ配信にカテゴリを追加します（認証必須）")
    @PostMapping("/livestreams/{liveStreamId}/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addCategoriesToLiveStream(
            @PathVariable UUID liveStreamId,
            @RequestBody List<UUID> categoryIds
    ) {
        categoryService.addCategoriesToLiveStream(liveStreamId, categoryIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "ライブ配信のカテゴリを置換", description = "指定ライブ配信のカテゴリを丸ごと置換します（認証必須）")
    @PutMapping("/livestreams/{liveStreamId}/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> replaceLiveStreamCategories(
            @PathVariable UUID liveStreamId,
            @RequestBody List<UUID> categoryIds
    ) {
        categoryService.replaceLiveStreamCategories(liveStreamId, categoryIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "ライブ配信からカテゴリを1つ外す", description = "指定ライブ配信から単一カテゴリを解除します（認証必須）")
    @DeleteMapping("/livestreams/{liveStreamId}/categories/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeCategoryFromLiveStream(
            @PathVariable UUID liveStreamId,
            @PathVariable UUID categoryId
    ) {
        categoryService.removeCategoryFromLiveStream(liveStreamId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // ========================================================
    // ============== Ⅲ. 管理者必須（Admin-only） =============
    // ========================================================

    @Operation(summary = "カテゴリ作成", description = "新しいカテゴリを作成します（管理者）")
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryCreateRequestDTO request
    ) {
        CategoryResponseDTO created = categoryService.createCategory(request);
        return ResponseEntity
                .created(URI.create("/api/categories/" + created.getId()))
                .body(created);
    }

    @Operation(summary = "カテゴリ更新", description = "カテゴリ名・説明を更新します（管理者）")
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "カテゴリ削除", description = "カテゴリを削除します（管理者）")
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
