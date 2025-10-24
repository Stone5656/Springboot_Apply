package com.example.dto.categories;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import java.util.UUID;
import org.hibernate.validator.constraints.UniqueElements;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiveStreamCategoriesAddRequest {
    @NotNull
    private UUID liveStreamId;

    @NotEmpty
    @UniqueElements
    private List<@NotNull UUID> categoryIds;
}
