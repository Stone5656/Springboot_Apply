package com.example.controller;

import com.example.dto.users.LoginRequest;
import com.example.dto.users.LoginResponse;
import com.example.dto.users.UserRegisterRequest;
import com.example.dto.users.UserResponseDTO;
import com.example.dto.users.UserUpdateRequestDTO;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "ユーザー関連API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // ユーザー登録
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRegisterRequest request) {
        User created = userService.registerUser(request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(created));
    }

    // ユーザー認証
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // ユーザー取得
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::fromEntity) // ★ ここが重要
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ユーザー更新
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
        @PathVariable UUID id,
        @Valid @RequestBody UserUpdateRequestDTO request
    ) {
        User updated = userService.changeProfileUser(id, request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updated)); // ★ ここも変換必要
    }

    // ユーザー削除
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
