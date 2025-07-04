package com.example.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserUpdateRequestDTO {

    @NotBlank(message = "名前は必須です")
    @Size(max = 30)
    private String name;

    @Size(max = 1024)
    private String profileImagePath;

    @Size(max = 1024)
    private String coverImagePath;

    @Size(max = 1000)
    private String bio;

    // --- getter / setter ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
