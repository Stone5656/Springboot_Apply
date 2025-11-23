package com.example.service;

import com.example.dto.users.*;
import com.example.entity.User;
import com.example.enums.UserRole;
import com.example.repository.UserRepository;
import com.example.security.JwtUtils;
import java.time.Duration;
import java.util.Base64;
import java.security.SecureRandom;
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

    private static final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final MailService mailService;

    // =========================================================
    // =============== Ⅰ. 未認証OK（Public） ==================
    // =========================================================

    /**
     * ユーザー登録処理（未認証OK）。
     */
    @Transactional
    public User registerUser(UserRegisterRequestDTO request) {
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
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByPrimaryEmailEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが間違っています"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが間違っています");
        }

        String token = jwtUtils.generateToken(user);
        return LoginResponseDTO.builder()
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
     * パスワードリセット要求（未ログイン / forgot-password 用）。
     *
     * <メールアドレスの存在有無にかかわらず、呼び出し元は成功レスポンスを返す想定です。
     * アカウントが見つかった場合のみ、内部的にトークン発行とメール送信処理を行います。
     */
    @Transactional
    public void requestPasswordReset(String rawEmail) {
        if (!StringUtils.hasText(rawEmail)) {
            throw new IllegalArgumentException("メールアドレスが空です");
        }

        userRepository.findByPrimaryEmailEmailIgnoreCase(rawEmail)
            .ifPresent(user -> {
                String rawToken = generateSecureToken();

                // DB にはハッシュ + 有効期限のみ保存する
                user.issuePasswordResetToken(rawToken, Duration.ofMinutes(30));

                userRepository.save(user);

                // メール送信
                mailService.sendPasswordResetMail(user, rawToken);
            });

        // メールアドレスが存在しない場合でも何もしない
        // （存在有無を推測されないようにするため）
    }

        /**
     * パスワードリセット実行（reset-password 用）。
     *
     * @param rawToken       生のトークン（メールリンクに含まれていた値）
     * @param newRawPassword 新しい平文パスワード
     */
    @Transactional
    public void resetPassword(String rawToken, String newRawPassword) {
        if (!StringUtils.hasText(rawToken)) {
            throw new IllegalArgumentException("トークンが空です");
        }
        if (!StringUtils.hasText(newRawPassword)) {
            throw new IllegalArgumentException("新しいパスワードが空です");
        }

        // トークンハッシュでユーザーを特定
        String tokenHash = User.hashPasswordResetToken(rawToken);
        User user = userRepository.findByPasswordResetTokenHash(tokenHash)
            .orElseThrow(() -> new IllegalArgumentException("無効なトークンです"));

        if (user.isPasswordResetTokenExpired()) {
            throw new IllegalArgumentException("トークンの有効期限が切れています");
        }

        // パスワードを更新し、トークンは使い捨てにする
        user.hashAndSetPassword(newRawPassword, passwordEncoder);
        user.clearPasswordResetToken();

        userRepository.save(user);
    }

    /**
     * パスワードリセットや remember-me 用のトークンに利用する乱数トークンを生成します。
     *
     * <p>OWASP Forgot Password Cheat Sheet では、少なくとも 128bit 以上のエントロピーを
     * 持つ予測不能なトークンを推奨しています。
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[32]; // 256bit
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
