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
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", columnDefinition = "BINARY(16)") // UUIDを効率的に格納
    private UUID id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Email // 形式チェック
    @NotBlank // 空文字やnull禁止
    @Size(max = 320)
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @UpdateTimestamp
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;// emailが変更されたらアップデートする

    // hashさせたい
    @Column(name = "password")
    private String password;

    @Column(name = "remember_token")
    private String rememberToken;

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
    private Boolean isStreamer=false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    public void hashAndSetPassword(String rawPassword, PasswordEncoder encoder) {
        Assert.hasText(rawPassword, "Password must not be empty");
        Assert.notNull(encoder, "PasswordEncoder must not be null");
        this.password = encoder.encode(rawPassword);
    }
}
