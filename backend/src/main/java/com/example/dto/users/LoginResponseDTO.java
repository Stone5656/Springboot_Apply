package com.example.dto.users;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class LoginResponseDTO {
    private String token;
    private UserResponseDTO user;
}
