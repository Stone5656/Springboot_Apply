package com.example.service;

import com.example.dto.users.LoginRequest;
import com.example.dto.users.LoginResponse;
import com.example.dto.users.UserRegisterRequest;
import com.example.dto.users.UserResponseDTO;
import com.example.dto.users.UserUpdateRequestDTO;
import com.example.enums.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public User registerUser(UserRegisterRequest request) {
        if (!StringUtils.hasText(request.getName())) {
        throw new IllegalArgumentException("名前は必須です");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("パスワードは必須です");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています");
        }

        User user = new User(
            request.getName(),
            request.getEmail()
        );

        user.hashAndSetPassword(request.getPassword(), passwordEncoder);
        user.userSetRole(Role.USER);

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
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

    @Transactional
    public User changeProfileUser(UUID id, UserUpdateRequestDTO request) {
        return userRepository.findById(id)
            .map(user -> {
                user.updateProfile(
                    request.getName(),
                    request.getProfileImagePath(),
                    request.getCoverImagePath(),
                    request.getBio()
                );
                return userRepository.save(user);
            })
            .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
    }
}
