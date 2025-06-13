package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;
import java.time.LocalDateTime;
import java.util.UUID;
    protected $fillable = [
        'name',
        'email',
        'password',
        // 追加したカラム
        'profile_image_path',
        'cover_image_path',
        'bio',
        'channel_name',
        'is_streamer',
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'email_verified_at' => 'datetime',
        'password' => 'hashed', // Laravel 10以降では自動的に処理されることが多い
        'is_streamer' => 'boolean', // 追加
    ];
}
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
    @Column(columnDefinition = "BINARY(16)") // UUIDを効率的に格納
    private UUID id;

    @Column(nullable = false)
    private String name;

    
}
