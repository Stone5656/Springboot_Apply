package com.example.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class PasswordChangeRequestDTO {

    @NotBlank(message = "現在のパスワードを入力してください")
    private String oldPassword;

    @NotBlank(message = "新しいパスワードを入力してください")
    private String newPassword;
}
