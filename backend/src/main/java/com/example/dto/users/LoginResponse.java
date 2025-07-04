package com.example.dto.users;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private UserResponseDTO user;
}
