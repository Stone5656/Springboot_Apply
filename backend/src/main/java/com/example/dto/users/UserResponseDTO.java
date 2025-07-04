package com.example.dto.users;

import java.util.UUID;
import com.example.entity.User;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private String profileImagePath;
    private String coverImagePath;
    private String bio;
    private String channelName;
    private Boolean isStreamer;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    // --- コンストラクタ ---
    public UserResponseDTO(UUID id, String name, String email,
                           String profileImagePath, String coverImagePath,
                           String bio, String channelName, Boolean isStreamer,
                           LocalDateTime createdAt, LocalDateTime updateAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImagePath = profileImagePath;
        this.coverImagePath = coverImagePath;
        this.bio = bio;
        this.channelName = channelName;
        this.isStreamer = isStreamer;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
    }

    // --- ゲッター ---
    // public UUID getId() { return id; }
    // public String getName() { return name; }
    // public String getEmail() { return email; }
    // public String getProfileImagePath() { return profileImagePath; }
    // public String getCoverImagePath() { return coverImagePath; }
    // public String getBio() { return bio; }
    // public String getChannelName() { return channelName; }
    // public Boolean getIsStreamer() { return isStreamer; }
    // public LocalDateTime getCreatedAt() { return createdAt; }
    // public LocalDateTime getUpdateAt() { return updateAt; }

    // --- Entityから変換する static factory ---
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getProfileImagePath(),
            user.getCoverImagePath(),
            user.getBio(),
            user.getChannelName(),
            user.getIsStreamer(),
            user.getCreatedAt(),
            user.getUpdateAt()
        );
    }
}
