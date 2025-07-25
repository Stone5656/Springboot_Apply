/**
 * 自動生成された UserEmail のレスポンスDTO
 */
package com.example.dto.user_emails;

import com.example.entity.UserEmail;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserEmailResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserEmailResponseDTO fromEntity(UserEmail entity) {
        return UserEmailResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
