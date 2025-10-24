package com.example.service;

import com.example.dto.users.*;
import com.example.entity.User;
import com.example.enums.UserRole;
import com.example.repository.UserRepository;
import com.example.security.JwtUtils;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * ユーザー関連のビジネスロジックを管理するサービスクラス。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String USER_NOT_FOUND = "ユーザーが見つかりません";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // =========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // =========================================================

    /**
     * ユーザー登録処理（未認証OK）。
     */
    @Transactional
    public User registerUser(UserRegisterRequest request) {
        if (!StringUtils.hasText(request.getName()))     throw new IllegalArgumentException("名前は必須です");
        if (!StringUtils.hasText(request.getEmail()))    throw new IllegalArgumentException("メールアドレスは必須です");
        if (!StringUtils.hasText(request.getPassword())) throw new IllegalArgumentException("パスワードは必須です");

        if (userRepository.findByPrimaryEmailEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています");
        }

        User user = new User(request.getName(), request.getEmail());
        user.hashAndSetPassword(request.getPassword(), passwordEncoder);
        user.userSetRole(UserRole.USER);
        return userRepository.save(user);
    }

    /**
     * ユーザーログイン処理（未認証OK）。
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByPrimaryEmailEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが間違っています"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが間違っています");
        }

        String token = jwtUtils.generateToken(user);
        return LoginResponse.builder()
                .token(token)
                .user(UserResponseDTO.fromEntity(user))
                .build();
    }

    // =========================================================
    // ============== Ⅱ. 認証必須（Authenticated） =============
    // =========================================================

    /**
     * プロフィール更新（本人）。
     */
    @Transactional
    public User updateUserProfile(UUID userId, UserUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.updateProfile(request.getName(), request.getProfileImagePath(),
                           request.getCoverImagePath(), request.getBio());
        return userRepository.save(user);
    }

    /**
     * パスワード変更（本人）。
     */
    @Transactional
    public void changePassword(UUID userId, PasswordChangeRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.changePassword(request.getOldPassword(), request.getNewPassword(), passwordEncoder);
        userRepository.save(user);
    }

    /**
     * メールアドレス変更（本人）。
     */
    @Transactional
    public void changeEmail(UUID userId, EmailChangeRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.changeEmail(request.getNewEmail());
        userRepository.save(user);
    }

    /**
     * 電話番号更新（本人）。
     */
    @Transactional
    public void updatePhoneNumber(UUID userId, PhoneNumberUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.updatePhoneNumber(request.getPhoneNumber());
        userRepository.save(user);
    }

    /**
     * 言語・タイムゾーン・誕生日の更新（本人）。
     */
    @Transactional
    public void updatePreferences(UUID userId, PreferenceUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.updatePreferences(request.getTimezone(), request.getLanguage(), request.getBirthday());
        userRepository.save(user);
    }

    /**
     * 論理削除（本人）。
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.softDelete();
        userRepository.save(user);
    }

    /**
     * リメンバートークン発行（本人）。
     */
    @Transactional
    public String issueRememberToken(UUID userId, Duration duration) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.issueRememberToken(duration);
        userRepository.save(user);
        return user.getRememberToken();
    }

    /**
     * リメンバートークン検証（本人）。
     */
    public boolean verifyRememberToken(UUID userId, String token) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        return user.verifyRememberToken(token);
    }

    /**
     * ログイン成功記録（本人）。
     */
    @Transactional
    public void markLoginSuccess(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.markLoginSuccess();
        userRepository.save(user);
    }

    /**
     * ログイン失敗記録（本人）。
     */
    @Transactional
    public void markLoginFailure(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.markLoginFailure();
        userRepository.save(user);
    }

    // =========================================================
    // ============== Ⅲ. 管理者必須（Admin-only） ===============
    // =========================================================

    /**
     * UUIDからユーザーを取得（管理用）。
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    /**
     * ユーザーの復元（管理用）。
     */
    @Transactional
    public void restoreUser(UUID userId) {
        User user = userRepository.findByIdIncludingDeleted(userId)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.restore();
        userRepository.save(user);
    }
}
