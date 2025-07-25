package com.example.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class PhoneNumberUpdateRequestDTO {

    @NotBlank(message = "電話番号を入力してください")
    @Pattern(regexp = "^0\\d{9,10}$", message = "電話番号の形式が正しくありません")
    private String phoneNumber;
}
