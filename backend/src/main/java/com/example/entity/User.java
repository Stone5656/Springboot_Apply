package com.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
// @Setter // ← 安易なSetterは原則として使用しない (不変性の確保)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPAのための protected コンストラクタ
@ToString(exclude = {"password"}) // パスワードはログに出力しない
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
// ドメインイベントを発行するために AbstractAggregateRoot を継承
public class User extends AbstractAggregateRoot<User> {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Email // 形式チェック
    @NotBlank // 空文字やnull禁止
    @Size(max = 320)
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt; // emailが変更されたらアップデートする

    // hashさせたい
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "remember_token")
    private String rememberToken;

    @Column(name = "remember_token_expires_at")
    private LocalDateTime rememberTokenExpiresAt;

    @Column(name = "profile_image_path")
    @Size(max = 1024)
    private String profileImagePath;

    @Column(name = "cover_image_path")
    @Size(max = 1024)
    private String coverImagePath;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "channel_name", unique = true, length = 30)
    private String channelName;

    @Column(name = "is_streamer", nullable = false)
    private Boolean isStreamer = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // === コンストラクタ ===
    public User(String name, String email) {
        Assert.hasText(name, "名前は必須です");
        Assert.hasText(email, "メールアドレスは必須です");
        this.name = name;
        this.email = email;
    }

    // === メソッド ===

    // パスワードを受け取り、ハッシュ化してpasswordフィールドに入れるメソッド
    public void hashAndSetPassword(String rawPassword, PasswordEncoder encoder) {
        Assert.hasText(rawPassword, "パスワード無いでっせ");
        Assert.notNull(encoder, "パスワードエンコーダー忘れてまっせ");
        this.password = encoder.encode(rawPassword);
    }

    public void userSetRole(
        Role role
    ) {
        this.role = role;
    }

    // プロフィール更新用
    public void updateProfile(
        String name,
        String profileImagePath,
        String coverImagePath,
        String bio
    ) {
        this.name = name;
        this.profileImagePath = profileImagePath;
        this.coverImagePath = coverImagePath;
        this.bio = bio;
    }

    public void changePassword(String oldPassword, String newPassword, PasswordEncoder encoder) {
        Assert.hasText(oldPassword, "古いパスワード無いでっせ");
        Assert.hasText(newPassword, "新しいパスワード無いでっせ");
        Assert.notNull(encoder, "パスワードエンコーダー忘れてまっせ");

        if (!encoder.matches(oldPassword, this.password)) {
            throw new IllegalArgumentException("パスワード間違ってて草");
        }

        this.password = encoder.encode(newPassword);
    }

    public void changeEmail(String newEmail) {
        Assert.hasText(newEmail, "メアド無いでっせ");

        if (!this.email.equals(newEmail)) {
            this.email = newEmail;
            this.emailVerifiedAt = null; // 再認証の必要あり
        }
    }

    public void issueRememberToken(Duration validDuration) {
        this.rememberToken = UUID.randomUUID().toString();
        this.rememberTokenExpiresAt = LocalDateTime.now().plus(validDuration);
    }

    public boolean verifyRememberToken(String token) {
        return this.rememberToken != null
            && this.rememberToken.equals(token)
            && this.rememberTokenExpiresAt != null
            && this.rememberTokenExpiresAt.isAfter(LocalDateTime.now());
    }

}
