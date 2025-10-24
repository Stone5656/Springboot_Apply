package com.example.dto.categories;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import java.util.UUID;
import org.hibernate.validator.constraints.UniqueElements;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoCategoriesAddRequest {
    @NotNull
    private UUID videoId;

    @NotEmpty
    @UniqueElements // 重複IDの混入を防ぐ（Hibernate Validator拡張）
    private List<@NotNull UUID> categoryIds;
}
