package com.example.dto.tags;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * タグ検索用リクエストDTO
 */
@Getter
@Builder
public class TagSearchRequestDTO {

    @Size(max = 100, message = "検索キーワードは100文字以内で入力してください。")
    private String keyword;
}
