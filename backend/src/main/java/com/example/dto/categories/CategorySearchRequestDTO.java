package com.example.dto.categories;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Builder;

/**
 * カテゴリ検索用リクエストDTO
 */
@Getter
@Builder
public class CategorySearchRequestDTO {

    @Size(max = 100, message = "検索キーワードは100文字以内で入力してください。")
    private String keyword;
}
