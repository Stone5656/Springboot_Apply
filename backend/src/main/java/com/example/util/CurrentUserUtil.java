package com.example.util;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserUtil {

    private final UserRepository userRepository;

    /**
     * 現在認証中のユーザーを取得します。
     *
     * @return User エンティティ
     * @throws IllegalStateException
     *             認証情報が取得できない場合
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("認証ユーザーが取得できません");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId()).orElseThrow(() -> new IllegalStateException("ユーザー情報が存在しません"));
    }
}
