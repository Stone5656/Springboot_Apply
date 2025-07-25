package com.example.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class EmailChangeRequestDTO {

    @NotBlank(message = "新しいメールアドレスを入力してください")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String newEmail;
}
