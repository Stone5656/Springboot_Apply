// com.example.security.UserPrincipal
package com.example.security;

import com.example.entity.User;
import com.example.enums.UserRole;
import java.util.*;
import lombok.Getter;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public final class UserPrincipal implements UserDetails {
    private final UUID id;
    private final String username;   // ← ここは生のメール文字列
    private final String password;
    private final UserRole role;

    public UserPrincipal(UUID id, String username, String password, UserRole role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username(email) は必須です");
        }
        if (role == null) {
            throw new IllegalArgumentException("role は必須です");
        }
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public static UserPrincipal from(User u) {
        String email = (u.getPrimaryEmail() != null) ? u.getPrimaryEmail().getEmail() : null;
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("User.primaryEmail.email が未設定です");
        }
        return new UserPrincipal(u.getId(), email, u.getPassword(), u.getRole());
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
    @Override public String getUsername() { return username; } // ← JPAに触らない
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
