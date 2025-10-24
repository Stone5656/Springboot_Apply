package com.example.dto.categories;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiveStreamCategoryRemoveRequest {
    @NotNull
    private UUID liveStreamId;

    @NotNull
    private UUID categoryId;
}
