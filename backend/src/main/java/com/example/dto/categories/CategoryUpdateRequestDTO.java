package com.example.dto.categories;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * PUT用（全体更新）にするなら name を @NotBlank 必須に。
 * PATCH的に使うなら両方null可とし、Serviceで "nullは未変更" に解釈。
 * ここでは PATCH寄り（柔軟）を採用。
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryUpdateRequestDTO {
    @Size(max = 100)
    private String name;         // nullなら変更なし

    @Size(max = 500)
    private String description;  // nullなら変更なし
}
