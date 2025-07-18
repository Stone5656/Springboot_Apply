package com.example.security;

import com.example.entity.User;
import com.example.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final Role role;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 必要に応じて変更
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 必要に応じて変更
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 必要に応じて変更
    }

    @Override
    public boolean isEnabled() {
        return true; // メール認証後などで切り替えてもOK
    }
}
