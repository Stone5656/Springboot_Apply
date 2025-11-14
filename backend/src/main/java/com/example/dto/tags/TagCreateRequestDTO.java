package com.example.dto.tags;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * タグ作成用リクエストDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagCreateRequestDTO {

    @NotBlank                // 空白のみ不可
    @Size(max = 100)         // 文字数制限（Category に合わせて短めに制限）
    private String name;

    @Size(max = 255)
    private String slug;
}
