package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

/**
 * ユーザーEmailエンティティクラス。
 * 複数のメールアドレスを登録するために使用される。
 * メソッド自体はシンプルでUserに影響を及ぼしにくいもののみに限定される
 *
 * @version 1.1
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"email"})
@Entity
@Table(name = "user_emails", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
public class UserEmail {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 320)
    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 作成日時 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新日時 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 主な情報を指定して初期化するコンストラクタ。
     *
     * @param email メールアドレス（320文字以内・必須）
     * @param user 関連付けるユーザー（必須）
     * @param isPrimary 主メールアドレスかどうかのフラグ
     */
    public UserEmail(String email, User user, boolean isPrimary) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (user == null) {
            throw new IllegalArgumentException("ユーザーは必須です");
        }
        this.email = email;
        this.user = user;
        this.isPrimary = isPrimary;
    }

    public void resetVerification() {
        this.verifiedAt = null;
    }
}
