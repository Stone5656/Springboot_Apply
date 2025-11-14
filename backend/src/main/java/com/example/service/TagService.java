package com.example.service;

import com.example.dto.tags.*;
import com.example.entity.LiveStream;
import com.example.entity.LiveStreamTag;
import com.example.entity.Tag;
import com.example.entity.Video;
import com.example.entity.VideoTag;
import com.example.repository.LiveStreamTagRepository;
import com.example.repository.TagRepository;
import com.example.repository.VideoTagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private static final String TAG_NOT_FOUND = "タグが見つかりません (ID: %s)";

    private final TagRepository tagRepository;
    private final LiveStreamTagRepository liveStreamTagRepository;
    private final VideoTagRepository videoTagRepository;

    @PersistenceContext
    private EntityManager em;


    // ========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // ========================================================

    public TagResponseDTO getTag(UUID id) {
        Tag tag = tagRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException(String.format(TAG_NOT_FOUND, id)));
        return TagResponseDTO.fromEntity(tag);
    }

    public Page<TagResponseDTO> searchTags(TagSearchRequestDTO req, Pageable pageable) {
        String keyword = (req != null && req.getKeyword() != null) ? req.getKeyword() : "";
        Page<Tag> page = (keyword.isBlank()) ? tagRepository.findAll(pageable)
                : tagRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return page.map(TagResponseDTO::fromEntity);
    }

    // ========================================================
    // ============ Ⅱ. 認証必須（Authenticated） ==============
    // ========================================================

    @Transactional
    public void addTagsToLiveStream(UUID liveStreamId, List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return;

        // 1) 入力タグの存在チェック
        Set<UUID> uniqueTags = new LinkedHashSet<>(tagIds);
        assertAllTagsExist(uniqueTags);

        // 2) 既存の関連を取得（派生クエリ：liveStreamId で検索）
        Set<UUID> alreadyTags = liveStreamTagRepository.findByLiveStream_Id(liveStreamId).stream()
                .map(lt -> lt.getTag().getId()).collect(Collectors.toSet());

        // 3) 未登録タグを挿入
        LiveStream liveStreamRef = em.getReference(LiveStream.class, liveStreamId);
        List<LiveStreamTag> toSave = uniqueTags.stream().filter(tagId -> !alreadyTags.contains(tagId))
                .map(tagId -> {
                    Tag tagRef = em.getReference(Tag.class, tagId);
                    return new LiveStreamTag(liveStreamRef, tagRef);
                }).toList();

        if (!toSave.isEmpty()) {
            liveStreamTagRepository.saveAll(toSave);
        }
    }

    @Transactional
    public void replaceLiveStreamTag(UUID liveStreamId, List<UUID> tagIds) {
        // 全削除 → 再挿入（nullなら全削除のみ）
        liveStreamTagRepository.deleteByLiveStream_Id(liveStreamId);
        if (tagIds == null)
            return;

        Set<UUID> uniqueTags = new LinkedHashSet<>(tagIds);
        if (uniqueTags.isEmpty())
            return;

        assertAllTagsExist(uniqueTags);

        LiveStream liveStreamRef = em.getReference(LiveStream.class, liveStreamId);
        List<LiveStreamTag> toSave = uniqueTags.stream().map(tagId -> {
            Tag tagRef = em.getReference(Tag.class, tagId);
            return new LiveStreamTag(liveStreamRef, tagRef);
        }).toList();

        liveStreamTagRepository.saveAll(toSave);
    }

    @Transactional
    public void removeTagFromLiveStream(UUID liveStreamId, UUID tagId) {
        liveStreamTagRepository.deleteByLiveStream_IdAndTag_Id(liveStreamId, tagId);
    }

    @Transactional
    public void addTagsToVideo(UUID videoId, List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return;

        // 1) 入力タグの存在チェック
        Set<UUID> uniqueTags = new LinkedHashSet<>(tagIds);
        assertAllTagsExist(uniqueTags);

        // 2) 既存の関連を取得（派生クエリ：videoId で検索）
        Set<UUID> alreadyTags = videoTagRepository.findByVideo_Id(videoId).stream()
                .map(lt -> lt.getTag().getId()).collect(Collectors.toSet());

        // 3) 未登録タグを挿入
        Video videoRef = em.getReference(Video.class, videoId);
        List<VideoTag> toSave = uniqueTags.stream().filter(tagId -> !alreadyTags.contains(tagId))
                .map(tagId -> {
                    Tag tagRef = em.getReference(Tag.class, tagId);
                    return new VideoTag(videoRef, tagRef);
                }).toList();

        if (!toSave.isEmpty()) {
            videoTagRepository.saveAll(toSave);
        }
    }

    @Transactional
    public void replaceVideoTag(UUID videoId, List<UUID> tagIds) {
        // 全削除 → 再挿入（nullなら全削除のみ）
        videoTagRepository.deleteByVideo_Id(videoId);
        if (tagIds == null)
            return;

        Set<UUID> uniqueTags = new LinkedHashSet<>(tagIds);
        if (uniqueTags.isEmpty())
            return;

        assertAllTagsExist(uniqueTags);

        Video videoRef = em.getReference(Video.class, videoId);
        List<VideoTag> toSave = uniqueTags.stream().map(tagId -> {
            Tag tagRef = em.getReference(Tag.class, tagId);
            return new VideoTag(videoRef, tagRef);
        }).toList();

        videoTagRepository.saveAll(toSave);
    }

    @Transactional
    public void removeTagFromVideo(UUID videoId, UUID tagId) {
        videoTagRepository.deleteByVideo_IdAndTag_Id(videoId, tagId);
    }

    // ========================================================
    // =================== Admin 操作 ========================
    // ========================================================

    @Transactional
    public TagResponseDTO createTag(TagCreateRequestDTO req) {
        tagRepository.findByNameIgnoreCase(req.getName()).ifPresent(x -> {
            throw new IllegalArgumentException("タグ名は既に存在します");
        });

        Tag tag = new Tag(req.getName(), req.getSlug());
        return TagResponseDTO.fromEntity(tagRepository.save(tag));
    }

    @Transactional
    public TagResponseDTO updateTag(UUID id, TagUpdateRequestDTO req) {
        Tag tag = tagRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException(String.format(TAG_NOT_FOUND, id)));

        if (req.getName() != null) {
            tagRepository.findByNameIgnoreCase(req.getName())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(x -> {
                        throw new IllegalArgumentException("タグ名は既に存在します");
                    });
            tag.setName(req.getName());
        }

        if (req.getSlug() != null) {
            tag.setSlug(req.getSlug());
        }

        return TagResponseDTO.fromEntity(tag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        liveStreamTagRepository.deleteByTag_Id(id);
        videoTagRepository.deleteByTag_Id(id);
        tagRepository.deleteById(id);
    }

    // ========================================================
    // ================== INTERNAL UTILITIES ==================
    // ========================================================

    private void assertAllTagsExist(Collection<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return;

        Set<UUID> tagIdSet = new LinkedHashSet<>(tagIds);
        List<Tag> foundTags = tagRepository.findAllById(tagIdSet);

        if (foundTags.size() != tagIdSet.size()) {
            Set<UUID> missingTags = new LinkedHashSet<>(tagIdSet);
            missingTags.removeAll(foundTags.stream().map(Tag::getId).collect(Collectors.toSet()));
            throw new NoSuchElementException("存在しないタグIDがあります: " + missingTags);
        }
    }
}
