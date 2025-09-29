package com.example.service;

import com.example.dto.users.*;
import com.example.entity.User;
import com.example.enums.UserRole;
import com.example.repository.UserRepository;
import com.example.security.JwtUtils;
import com.example.util.CurrentUserUtil;
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
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class UserService {

    private static final String USER_NOT_FOUND = "ユーザーが見つかりません";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CurrentUserUtil currentUserUtil;

    // ============================================
    // =============== 登録 / 認証 ================
    // ============================================

    /**
     * ユーザー登録処理。
     *
     * @param request
     *            登録リクエストDTO
     * @return 登録されたUserエンティティ
     * @throws IllegalArgumentException
     *             入力が不正な場合
     */
    @Transactional
    public User registerUser(UserRegisterRequest request) {
        if (!StringUtils.hasText(request.getName()))
            throw new IllegalArgumentException("名前は必須です");
        if (!StringUtils.hasText(request.getEmail()))
            throw new IllegalArgumentException("メールアドレスは必須です");
        if (!StringUtils.hasText(request.getPassword()))
            throw new IllegalArgumentException("パスワードは必須です");

        if (userRepository.findByPrimaryEmailEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています");
        }

        User user = new User(request.getName(), request.getEmail());
        user.hashAndSetPassword(request.getPassword(), passwordEncoder);
        user.userSetRole(UserRole.USER);
        return userRepository.save(user);
    }

    /**
     * ユーザーログイン処理。
     *
     * @param request
     *            ログインリクエスト
     * @return JWTトークンとユーザー情報
     * @throws IllegalArgumentException
     *             認証失敗時
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByPrimaryEmailEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが間違っています"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが間違っています");
        }

        String token = jwtUtils.generateToken(user);
        return LoginResponse.builder().token(token).user(UserResponseDTO.fromEntity(user)).build();
    }

    // ============================================
    // ============== プロフィール関連 ============
    // ============================================

    /**
     * 現在ログイン中のユーザーのプロフィールを更新。
     *
     * @param request
     *            更新内容DTO
     * @return 更新後のユーザー
     */
    @Transactional
    public User updateCurrentUserProfile(UserUpdateRequestDTO request) {
        User user = currentUserUtil.getCurrentUser();
        user.updateProfile(request.getName(), request.getProfileImagePath(), request.getCoverImagePath(),
                request.getBio());
        return userRepository.save(user);
    }

    // ============================================
    // =============== 認証ユーザー変更 ============
    // ============================================

    /**
     * パスワード変更処理。
     *
     * @param request
     *            パスワード変更リクエスト
     * @throws IllegalArgumentException
     *             不正なパスワード
     */
    @Transactional
    public void changePassword(PasswordChangeRequestDTO request) {
        User user = currentUserUtil.getCurrentUser();
        user.changePassword(request.getOldPassword(), request.getNewPassword(), passwordEncoder);
        userRepository.save(user);
    }

    /**
     * メールアドレスの変更。
     *
     * @param request
     *            メールアドレス変更リクエスト
     * @throws IllegalArgumentException
     *             無効なメールアドレス
     */
    @Transactional
    public void changeEmail(EmailChangeRequestDTO request) {
        User user = currentUserUtil.getCurrentUser();
        user.changeEmail(request.getNewEmail());
        userRepository.save(user);
    }

    /**
     * 電話番号の更新。
     *
     * @param request
     *            電話番号変更リクエスト
     * @throws IllegalArgumentException
     *             ユーザーが存在しない場合
     */
    @Transactional
    public void updatePhoneNumber(PhoneNumberUpdateRequestDTO request) {
        User user = currentUserUtil.getCurrentUser();
        user.updatePhoneNumber(request.getPhoneNumber());
        userRepository.save(user);
    }

    /**
     * 言語・タイムゾーン・誕生日の更新。
     *
     * @param request
     *            設定更新リクエスト
     */
    @Transactional
    public void updatePreferences(PreferenceUpdateRequestDTO request) {
        User user = currentUserUtil.getCurrentUser();
        user.updatePreferences(request.getTimezone(), request.getLanguage(), request.getBirthday());
        userRepository.save(user);
    }

    // ============================================
    // =============== ユーザー状態管理 ============
    // ============================================

    /**
     * ユーザーの論理削除。
     */
    @Transactional
    public void deleteCurrentUser() {
        User user = currentUserUtil.getCurrentUser();
        user.softDelete();
        userRepository.save(user);
    }

    /**
     * ユーザーの復元。
     *
     * @param id
     *            対象ユーザーID
     * @throws IllegalStateException
     *             未削除の場合
     */
    @Transactional
    public void restoreUser(UUID id) {
        User user = userRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        user.restore();
        userRepository.save(user);
    }

    // ============================================
    // =============== トークン関連 ================
    // ============================================

    /**
     * リメンバートークンの発行。
     *
     * @param duration
     *            トークン有効期間
     * @return トークン文字列
     */
    @Transactional
    public String issueRememberToken(Duration duration) {
        User user = currentUserUtil.getCurrentUser();
        user.issueRememberToken(duration);
        userRepository.save(user);
        return user.getRememberToken();
    }

    /**
     * リメンバートークンの検証。
     *
     * @param token
     *            トークン
     * @return 有効かどうか
     */
    public boolean verifyRememberToken(String token) {
        User user = currentUserUtil.getCurrentUser();
        return user.verifyRememberToken(token);
    }

    // ============================================
    // =============== ログイン記録 ================
    // ============================================

    /**
     * ログイン成功記録。
     */
    @Transactional
    public void markLoginSuccess() {
        User user = currentUserUtil.getCurrentUser();
        user.markLoginSuccess();
        userRepository.save(user);
    }

    /**
     * ログイン失敗記録。
     */
    @Transactional
    public void markLoginFailure() {
        User user = currentUserUtil.getCurrentUser();
        user.markLoginFailure();
        userRepository.save(user);
    }

    // ============================================
    // ================ 補助関数 ==================
    // ============================================

    /**
     * UUIDからユーザーを取得。
     *
     * @param id
     *            ユーザーID
     * @return ユーザーエンティティ
     * @throws IllegalArgumentException
     *             ユーザーが存在しない場合
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    /**
     * 現在ログイン中のユーザーを取得。
     *
     * @return ユーザーエンティティ
     * @throws IllegalStateException
     *             認証情報が取得できない、またはユーザーが存在しない場合
     */
    public User getCurrentUser() {
        return currentUserUtil.getCurrentUser();
    }
}
