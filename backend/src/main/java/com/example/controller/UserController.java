package com.example.controller;

import com.example.dto.users.LoginRequest;
import com.example.dto.users.LoginResponse;
import com.example.dto.users.UserRegisterRequest;
import com.example.dto.users.UserResponseDTO;
import com.example.dto.users.UserUpdateRequestDTO;
import com.example.entity.User;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "ユーザー関連API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * ユーザー登録
     */
    @Operation(summary = "ユーザー登録", description = "新しいユーザーを登録します")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRegisterRequest request) {
        User created = userService.registerUser(request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(created));
    }

    /**
     * ログイン
     */
    @Operation(summary = "ユーザーログイン", description = "メールアドレスとパスワードでログインし、JWTトークンを返します")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ユーザー取得
     */
    @Operation(summary = "ユーザー取得", description = "IDで特定のユーザーを取得します")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@Parameter(description = "ユーザーID") @PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    /**
     * ユーザー更新
     */
    @Operation(summary = "ユーザー情報更新", description = "プロフィール情報を更新します")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@Parameter(description = "ユーザーID") @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        User updated = userService.changeProfileUser(id, request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updated));
    }

    /**
     * ユーザー削除
     */
    @Operation(summary = "ユーザー削除", description = "指定したユーザーを削除します")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "ユーザーID") @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * パスワード変更
     */
    @Operation(summary = "パスワード変更", description = "古いパスワードと新しいパスワードを指定して変更します")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@Parameter(description = "ユーザーID") @PathVariable UUID id,
            @RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    /**
     * メールアドレス変更
     */
    @Operation(summary = "メールアドレス変更", description = "新しいメールアドレスに変更します")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/email")
    public ResponseEntity<Void> changeEmail(@Parameter(description = "ユーザーID") @PathVariable UUID id,
            @RequestParam String newEmail) {
        userService.changeEmail(id, newEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Remember Token 発行
     */
    @Operation(summary = "Remember Token 発行", description = "ワンタイムパスワード用のRememberTokenを発行します")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/remember-token")
    public ResponseEntity<String> issueRememberToken(@Parameter(description = "ユーザーID") @PathVariable UUID id,
            @RequestParam(defaultValue = "PT15M") String duration) {
        Duration validDuration = Duration.parse(duration);
        String token = userService.issueRememberToken(id, validDuration);
        return ResponseEntity.ok(token);
    }

    /**
     * Remember Token 検証
     */
    @Operation(summary = "Remember Token 検証", description = "RememberTokenが有効か検証します")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}/remember-token/verify")
    public ResponseEntity<Boolean> verifyRememberToken(@Parameter(description = "ユーザーID") @PathVariable UUID id,
            @RequestParam String token) {
        boolean valid = userService.verifyRememberToken(id, token);
        return ResponseEntity.ok(valid);
    }
}
