package com.example.dto.videos;

import java.util.UUID;
import com.example.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploaderDTO {
    private UUID id;
    private String name;
    private String profileImagePath;

    public static UploaderDTO fromUser(User user) {
        return UploaderDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .profileImagePath(user.getProfileImagePath())
                .build();
    }
}
