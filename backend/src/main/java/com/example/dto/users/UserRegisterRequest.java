package com.example.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "名前は必須です")
    @Size(max = 30)
    private String name;

    @NotBlank(message = "メールアドレスは必須です")
    @Email
    @Size(max = 320)
    private String email;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上にしてください")
    private String password;
}
