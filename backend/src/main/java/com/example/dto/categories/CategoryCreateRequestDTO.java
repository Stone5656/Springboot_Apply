package com.example.dto.categories;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryCreateRequestDTO {
    @NotBlank                // 空白のみ不可
    @Size(max = 100)         // 文字数制限
    private String name;

    @Size(max = 500)
    private String description; // 任意
}
