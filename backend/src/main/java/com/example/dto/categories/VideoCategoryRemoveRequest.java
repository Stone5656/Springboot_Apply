package com.example.dto.categories;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoCategoryRemoveRequest {
    @NotNull
    private UUID videoId;

    @NotNull
    private UUID categoryId;
}
