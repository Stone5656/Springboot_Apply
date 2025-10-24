package com.example.repository;

import com.example.entity.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // 正確一致（大文字小文字無視）
    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // あいまい検索 + ページング
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
