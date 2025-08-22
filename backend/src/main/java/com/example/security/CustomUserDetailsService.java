package com.example.security;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // primaryEmail.email を辿るメソッドに変更
        User user = userRepository.findByPrimaryEmailEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("指定のメールアドレスは登録されていません"));

        // ここでアカウント有効性チェック等が必要なら実施（例：isEnabled, isLocked など）
        return new UserPrincipal(user);
    }
}
