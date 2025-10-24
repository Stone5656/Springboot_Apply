// src/main/java/com/example/controller/UserController.java
package com.example.controller;

import com.example.dto.users.*;
import com.example.entity.User;
import com.example.security.UserPrincipal;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "ユーザー関連API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // クラス既定: APIとしては認証前提（公開APIは個別にPermitAll想定）
public class UserController {

    private final UserService userService;

    // ===== Auth (公開API: register / login) =====

    /** ユーザー登録 */
    @Operation(summary = "ユーザー登録", description = "新しいユーザーを登録します")
    @PostMapping("/register") // ※ 公開API（YAMLで PERMIT_ALL を付与）
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRegisterRequestDTO request) {
        User created = userService.registerUser(request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(created));
    }

    /** ログイン */
    @Operation(summary = "ユーザーログイン", description = "メールアドレスとパスワードでログインし、JWTトークンを返します")
    @PostMapping("/login") // ※ 公開API（YAMLで PERMIT_ALL を付与）
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // ===== Me（本人情報: 要認証） =====

    /** ログイン中のユーザー取得 */
    @Operation(summary = "自分のユーザー情報取得", description = "ログイン中のユーザーの情報を取得します")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal
    ) {
        return getUser(principal, principal.getId()); // 内部的に同じメソッドへ委譲（エイリアス化）
    }

    /** 自分の情報更新 */
    @Operation(summary = "ユーザー情報更新", description = "ログイン中のユーザーのプロフィール情報を更新します")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUser(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UserUpdateRequestDTO request
    ) {
        User updated = userService.updateUserProfile(principal.getId(), request);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updated));
    }

    /** 自分を削除 */
    @Operation(summary = "ユーザー削除", description = "ログイン中のユーザーを削除します")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteUser(principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ===== Admin-only（管理操作） =====

    @GetMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUser(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID id
    ) {
      return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.getUserById(id)));
    }

    /** ユーザー復元（ADMIN） */
    @Operation(summary = "ユーザー復元", description = "論理削除されたユーザーを復元します")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/restore") // ※ YAML: HAS_ROLE ADMIN
    public ResponseEntity<Void> restoreUser(@PathVariable UUID id) {
        userService.restoreUser(id);
        return ResponseEntity.ok().build();
    }

    // ===== Account Security（本人の資格情報・トークン: 要認証） =====

    /** パスワード変更 */
    @Operation(summary = "パスワード変更", description = "古いパスワードと新しいパスワードを指定して変更します")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody PasswordChangeRequestDTO request
    ) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

    /** メールアドレス変更 */
    @Operation(summary = "メールアドレス変更", description = "新しいメールアドレスに変更します")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/email")
    public ResponseEntity<Void> changeEmail(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody EmailChangeRequestDTO request
    ) {
        userService.changeEmail(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

    /** Remember Token 発行 */
    @Operation(summary = "Remember Token 発行", description = "RememberTokenを発行します")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/remember-token")
    public ResponseEntity<String> issueRememberToken(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(defaultValue = "PT15M") String duration
    ) {
        Duration validDuration = Duration.parse(duration);
        String token = userService.issueRememberToken(principal.getId(), validDuration);
        return ResponseEntity.ok(token);
    }

    /** Remember Token 検証 */
    @Operation(summary = "Remember Token 検証", description = "RememberTokenが有効か検証します")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/remember-token/verify")
    public ResponseEntity<Boolean> verifyRememberToken(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam String token
    ) {
        boolean valid = userService.verifyRememberToken(principal.getId(), token);
        return ResponseEntity.ok(valid);
    }

    // ===== Preferences / Contact（本人の設定: 要認証） =====

    /** 環境設定更新 */
    @Operation(summary = "環境設定更新", description = "タイムゾーン・言語・誕生日を更新します")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/preferences")
    public ResponseEntity<Void> updatePreferences(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody PreferenceUpdateRequestDTO request
    ) {
        userService.updatePreferences(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

    /** 電話番号変更 */
    @Operation(summary = "電話番号変更", description = "電話番号を変更します")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/phone")
    public ResponseEntity<Void> updatePhoneNumber(
        @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody PhoneNumberUpdateRequestDTO request
    ) {
        userService.updatePhoneNumber(principal.getId(), request);
        return ResponseEntity.ok().build();
    }
}
