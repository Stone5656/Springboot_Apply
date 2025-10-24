package com.example.dto.categories;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import java.util.UUID;
import org.hibernate.validator.constraints.UniqueElements;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoCategoriesReplaceRequest {
    @NotNull
    private UUID videoId;

    @NotNull // 空配列は「カテゴリ0件」を意味→削除として扱う
    @UniqueElements
    private List<@NotNull UUID> categoryIds;
}
