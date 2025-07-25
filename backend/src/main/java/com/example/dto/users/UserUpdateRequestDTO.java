package com.example.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
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
}
