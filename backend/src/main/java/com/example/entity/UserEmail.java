package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.example.util.entity.AbstractBaseEntity;

/**
 * ユーザーEmailエンティティクラス。
 * 複数のメールアドレスを登録するために使用される。
 * メソッド自体はシンプルでUserに影響を及ぼしにくいもののみに限定される
 *
 * @version 1.2
 */

@Entity
@Table(name = "user_emails", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"email"})
public class UserEmail extends AbstractBaseEntity {

    @Email
    @NotBlank
    @Size(max = 320)
    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 主な情報を指定して初期化するコンストラクタ。
     *
     * @param email メールアドレス（320文字以内・必須）
     * @param user 関連付けるユーザー（必須）
     */
    public UserEmail(String email, User user) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (user == null) {
            throw new IllegalArgumentException("ユーザーは必須です");
        }
        this.email = email;
        this.user = user;
    }

    public void resetVerification() {
        this.verifiedAt = null;
    }

    public void setUser(User user) {
        this.user = user;
        // 逆側へもつなぐ（重複追加防止は必要ならチェック）
        if (user != null && !user.getEmails().contains(this)) {
            user.getEmails().add(this);
        }
    }
}
