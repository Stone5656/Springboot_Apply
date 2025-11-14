package com.example.repository;

import com.example.entity.Tag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Tag エンティティ用リポジトリ。
 * CategoryRepository と同じ検索インターフェースを提供します。
 */
public interface TagRepository extends JpaRepository<Tag, UUID> {

    // 正確一致（大文字小文字無視）
    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // あいまい検索 + ページング
    Page<Tag> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
