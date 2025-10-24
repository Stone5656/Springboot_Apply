package com.example.service;

import com.example.dto.categories.*;
import com.example.entity.Category;
import com.example.entity.LiveStream;
import com.example.entity.VideoCategory;
import com.example.entity.LiveStreamCategory;
import com.example.entity.Video;
import com.example.repository.CategoryRepository;
import com.example.repository.VideoCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.example.repository.LiveStreamCategoryRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND = "カテゴリが見つかりません (ID: %s)";

    private final CategoryRepository categoryRepository;
    private final VideoCategoryRepository videoCategoryRepository;
    private final LiveStreamCategoryRepository liveStreamCategoryRepository;

    @PersistenceContext
    private EntityManager em;

    // ========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // ========================================================

    public CategoryResponseDTO getCategory(UUID id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(String.format(CATEGORY_NOT_FOUND, id)));
        return CategoryResponseDTO.fromEntity(c);
    }

    public Page<CategoryResponseDTO> searchCategories(CategorySearchRequestDTO req, Pageable pageable) {
        String keyword = (req != null && req.getKeyword() != null) ? req.getKeyword() : "";
        Page<Category> page = (keyword.isBlank())
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return page.map(CategoryResponseDTO::fromEntity);
    }

    // ========================================================
    // ============ Ⅱ. 認証必須（Authenticated） ==============
    // ========================================================

    // ---- Video × Category ----
    @Transactional
    public void addCategoriesToVideo(UUID videoId, List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        // 1) 入力カテゴリの存在チェック
        Set<UUID> unique = new LinkedHashSet<>(categoryIds);
        assertAllCategoriesExist(unique);

        // 2) 既存の関連を取得（派生クエリ：video.id で検索）
        Set<UUID> already = videoCategoryRepository.findByVideo_Id(videoId).stream()
                .map(vc -> vc.getCategory().getId())
                .collect(Collectors.toSet());

        // 3) 参照プロキシを使って未登録分だけ挿入
        Video videoRef = em.getReference(Video.class, videoId); // SELECTしない参照
        List<VideoCategory> toSave = unique.stream()
                .filter(cid -> !already.contains(cid))
                .map(cid -> {
                    Category catRef = em.getReference(Category.class, cid);
                    return new VideoCategory(catRef, videoRef); // ★ (Category, Video) コンストラクタ
                })
                .toList();

        if (!toSave.isEmpty()) {
            videoCategoryRepository.saveAll(toSave);
        }
    }

    @Transactional
    public void replaceVideoCategories(UUID videoId, List<UUID> categoryIds) {
        // 全削除 → 再挿入（nullなら全削除のみ）
        videoCategoryRepository.deleteByVideo_Id(videoId);
        if (categoryIds == null) return;

        Set<UUID> unique = new LinkedHashSet<>(categoryIds);
        if (unique.isEmpty()) return;
        assertAllCategoriesExist(unique);

        Video videoRef = em.getReference(Video.class, videoId); // SELECTしない参照
        List<VideoCategory> toSave = unique.stream()
                .map(cid -> {
                    Category catRef = em.getReference(Category.class, cid);
                    return new VideoCategory(catRef, videoRef); // ★ (Category, Video) コンストラクタ
                })
                .toList();

        videoCategoryRepository.saveAll(toSave);
    }

    @Transactional
    public void removeCategoryFromVideo(UUID videoId, UUID categoryId) {
        videoCategoryRepository.deleteByVideo_IdAndCategory_Id(videoId, categoryId);
    }

    // ---- LiveStream × Category ----

    @Transactional
    public void addCategoriesToLiveStream(UUID liveStreamId, List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        Set<UUID> unique = new LinkedHashSet<>(categoryIds);
        assertAllCategoriesExist(unique);

        Set<UUID> already = liveStreamCategoryRepository.findByLiveStream_Id(liveStreamId).stream()
                .map(lc -> lc.getCategory().getId())
                .collect(Collectors.toSet());

        LiveStream liveRef = em.getReference(LiveStream.class, liveStreamId); // SELECTしない参照
        List<LiveStreamCategory> toSave = unique.stream()
                .filter(cid -> !already.contains(cid))
                .map(cid -> {
                    Category catRef = em.getReference(Category.class, cid);
                    return new LiveStreamCategory(catRef, liveRef); // ★ (Category, LiveStream) コンストラクタ
                })
                .toList();

        if (!toSave.isEmpty()) {
            liveStreamCategoryRepository.saveAll(toSave);
        }
    }

    @Transactional
    public void replaceLiveStreamCategories(UUID liveStreamId, List<UUID> categoryIds) {
        liveStreamCategoryRepository.deleteByLiveStream_Id(liveStreamId);
        if (categoryIds == null) return;

        Set<UUID> unique = new LinkedHashSet<>(categoryIds);
        if (unique.isEmpty()) return;
        assertAllCategoriesExist(unique);

        LiveStream liveRef = em.getReference(LiveStream.class, liveStreamId); // SELECTしない参照
        List<LiveStreamCategory> toSave = unique.stream()
                .map(cid -> {
                    Category catRef = em.getReference(Category.class, cid);
                    return new LiveStreamCategory(catRef, liveRef); // ★ (Category, LiveStream) コンストラクタ
                })
                .toList();

        liveStreamCategoryRepository.saveAll(toSave);
    }

    @Transactional
    public void removeCategoryFromLiveStream(UUID liveStreamId, UUID categoryId) {
        liveStreamCategoryRepository.deleteByLiveStream_IdAndCategory_Id(liveStreamId, categoryId);
    }

    // ========================================================
    // ============== Ⅲ. 管理者必須（Admin-only） =============
    // ========================================================

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO req) {
        categoryRepository.findByNameIgnoreCase(req.getName())
                .ifPresent(x -> { throw new IllegalArgumentException("カテゴリ名は既に存在します"); });

        Category c = new Category(req.getName(), req.getDescription(), null);
        return CategoryResponseDTO.fromEntity(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(UUID id, CategoryUpdateRequestDTO req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(String.format(CATEGORY_NOT_FOUND, id)));

        if (req.getName() != null) {
            categoryRepository.findByNameIgnoreCase(req.getName())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(x -> { throw new IllegalArgumentException("カテゴリ名は既に存在します"); });
            c.setName(req.getName());
        }
        if (req.getDescription() != null) {
            c.setDescription(req.getDescription());
        }
        return CategoryResponseDTO.fromEntity(c); // 永続化コンテキストで更新
    }

    @Transactional
    public void deleteCategory(UUID id) {
        // 参照掃除（DBでFK CASCADEを使わない方針ならこちらで）
        videoCategoryRepository.deleteByCategory_Id(id);
        liveStreamCategoryRepository.deleteByCategory_Id(id);
        categoryRepository.deleteById(id);
    }

    // ========================================================
    // ================== INTERNAL UTILITIES ==================
    // ========================================================

    private void assertAllCategoriesExist(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return;
        Set<UUID> set = new LinkedHashSet<>(ids);
        List<Category> found = categoryRepository.findAllById(set);
        if (found.size() != set.size()) {
            Set<UUID> hit = found.stream().map(Category::getId).collect(Collectors.toSet());
            set.removeAll(hit);
            throw new NoSuchElementException("存在しないカテゴリIDがあります: " + set);
        }
    }
}
