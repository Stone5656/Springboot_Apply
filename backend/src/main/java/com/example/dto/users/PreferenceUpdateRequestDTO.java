package com.example.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class PreferenceUpdateRequestDTO {

    @NotBlank(message = "タイムゾーンを指定してください")
    private String timezone;

    @NotBlank(message = "言語コードを指定してください")
    private String language;

    @Past(message = "生年月日は過去の日付を指定してください")
    private LocalDate birthday;
}
