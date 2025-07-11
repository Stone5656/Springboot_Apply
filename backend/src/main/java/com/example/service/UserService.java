package com.example.service;

import com.example.dto.users.LoginRequest;
import com.example.dto.users.LoginResponse;
import com.example.dto.users.UserRegisterRequest;
import com.example.dto.users.UserResponseDTO;
import com.example.dto.users.UserUpdateRequestDTO;
import com.example.entity.User;
import com.example.enums.Role;
import com.example.repository.UserRepository;
import com.example.security.JwtUtils;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String USER_NOT_FOUND = "ユーザーが見つかりません";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * ユーザー登録
     */
    @Transactional
    public User registerUser(UserRegisterRequest request) {
        if (!StringUtils.hasText(request.getName()))
            throw new IllegalArgumentException("名前は必須です");
        if (!StringUtils.hasText(request.getEmail()))
            throw new IllegalArgumentException("メールアドレスは必須です");
        if (!StringUtils.hasText(request.getPassword()))
            throw new IllegalArgumentException("パスワードは必須です");

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています");
        }

        User user = new User(request.getName(), request.getEmail());
        user.hashAndSetPassword(request.getPassword(), passwordEncoder);
        user.userSetRole(Role.USER);

        return userRepository.save(user);
    }

    /**
     * ログイン
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが間違っています"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが間違っています");
        }

        String token = jwtUtils.generateToken(user);

        return LoginResponse.builder().token(token).user(UserResponseDTO.fromEntity(user)).build();
    }

    /**
     * プロフィール更新
     */
    @Transactional
    public User changeProfileUser(UUID id, UserUpdateRequestDTO request) {
        return userRepository.findById(id).map(user -> {
            user.updateProfile(request.getName(), request.getProfileImagePath(), request.getCoverImagePath(),
                    request.getBio());
            return userRepository.save(user);
        }).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    /**
     * ユーザー削除
     */
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException(USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    /**
     * パスワード変更
     */
    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        user.changePassword(oldPassword, newPassword, passwordEncoder);
        userRepository.save(user);
    }

    /**
     * メールアドレス変更
     */
    @Transactional
    public void changeEmail(UUID userId, String newEmail) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        user.changeEmail(newEmail);
        userRepository.save(user);
    }

    /**
     * RememberToken発行
     */
    @Transactional
    public String issueRememberToken(UUID userId, Duration validDuration) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        user.issueRememberToken(validDuration);
        userRepository.save(user);
        return user.getRememberToken();
    }

    /**
     * RememberToken検証
     */
    public boolean verifyRememberToken(UUID userId, String token) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        return user.verifyRememberToken(token);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }
}
