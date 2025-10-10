package com.example.enums;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ADMIN("ROLE_ADMIN", 3),
    MODERATOR("ROLE_MODERATOR", 2),
    USER("ROLE_USER", 1);

    private final String authority;
    private final int level; // ヒエラルキー比較用

    UserRole(String authority, int level) {
        this.authority = authority;
        this.level = level;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public int getLevel() {
        return level;
    }

    /** 上位ロールが下位を包含する場合の判定 */
    public boolean isHigherThan(UserRole other) {
        return this.level > other.level;
    }
}
