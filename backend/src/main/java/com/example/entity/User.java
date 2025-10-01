package com.example.entity;

import static com.example.util.DeletionUtils.*;

import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.util.entity.AbstractSoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * ユーザーエンティティクラス。 このクラスはアプリケーションのユーザー情報を保持し、JPAを介してDBとマッピングされます。
 * パスワードのハッシュ化、トークンの発行、プロフィール・パスワード・メールの更新等のドメインロジックも含みます。
 *
 * @version 1.2
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"password", "rememberToken", "primaryEmail", "phoneNumber"})
@Entity
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_primary_email_id", columnList = "primary_email_id",
                        unique = true),
                @Index(name = "idx_user_name", columnList = "name", unique = true),
                @Index(name = "idx_user_last_login", columnList = "last_login_at"),
                @Index(name = "idx_user_status", columnList = "status")})
public class User extends AbstractSoftDeletableEntity {

    /** ユーザー名（30文字以内） */
    @NotBlank
    @Size(max = 30)
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    /** 主メールアドレス（UserEmail とのリレーション） */
    @OneToMany(mappedBy = "user",
               cascade = {CascadeType.PERSIST, CascadeType.MERGE},
               orphanRemoval = true)
    private List<UserEmail> emails = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, optional = true,
          cascade = {CascadeType.PERSIST, CascadeType.MERGE},
          orphanRemoval = true)
    @JoinColumn(name = "primary_email_id", nullable = true, unique = true)
    private UserEmail primaryEmail;

    /** メールアドレス認証日時 */
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    /** ハッシュ化されたパスワード */
    @NotBlank
    @Size(min = 60, max = 255)
    @Column(name = "password", nullable = false)
    private String password;

    /** リメンバートークン */
    @Size(max = 255)
    @Column(name = "remember_token")
    private String rememberToken;

    /** リメンバートークン有効期限 */
    @Column(name = "remember_token_expires_at")
    private LocalDateTime rememberTokenExpiresAt;

    /** プロフィール画像のファイルパス */
    @Size(max = 1024)
    @Column(name = "profile_image_path")
    private String profileImagePath;

    /** カバー画像のファイルパス */
    @Size(max = 1024)
    @Column(name = "cover_image_path")
    private String coverImagePath;

    /** 自己紹介（最大1000文字） */
    @Size(max = 1000)
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /** チャンネル名 */
    @Size(max = 30)
    @Column(name = "channel_name", length = 30)
    private String channelName;

    /** 配信者フラグ */
    @NotNull
    @Column(name = "is_stream", nullable = false)
    private Boolean isStream = false;

    /** ユーザーのロール（ADMIN / USER / MODERATOR など） */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    /** ステータス（ACTIVE / SUSPENDED / DELETED） */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /** 最終ログイン日時 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** ログイン失敗回数 */
    @Column(name = "login_failure_count", nullable = false)
    private int loginFailureCount = 0;

    /** タイムゾーン（例: Asia/Tokyo） */
    @Size(max = 100)
    @Column(name = "timezone")
    private String timezone;

    /** 表示言語（例: ja, en） */
    @Size(max = 10)
    @Column(name = "language")
    private String language;

    /** 生年月日 */
    @Column(name = "birthday")
    private LocalDate birthday;

    /** 電話番号 */
    @Size(max = 30)
    @Column(name = "phone_number")
    private String phoneNumber;

    // ============================
    // ======== リレーション ========
    // ============================

    /** メールアドレスとの関連（論理削除対応、主メール含む） */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Filter(name = "activeFilter", condition = "deleted_at IS NULL")
    private List<UserEmail> userEmails = new ArrayList<>();

    /** 配信との関連（論理削除対応） */
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Filter(name = "activeFilter", condition = "deleted_at IS NULL")
    private List<LiveStream> liveStreams = new ArrayList<>();

    /** アーカイブ動画との関連（物理削除） */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Video> videos = new ArrayList<>();

    /** チャットメッセージとの関連（物理削除） */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    /** フォローしている購読情報（物理削除） */
    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Subscription> following = new ArrayList<>();

    /** フォローされている購読情報（物理削除） */
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Subscription> followers = new ArrayList<>();

    // ===================================================
    // =============== ユーザー基本情報の操作 ===============
    // ===================================================

    /**
     * ユーザーの電話番号を更新します。
     *
     * @param newPhoneNumber 新しい電話番号（null不可、最大30文字）
     * @throws IllegalArgumentException 電話番号が空の場合
     */
    public void updatePhoneNumber(String newPhoneNumber) {
        Assert.hasText(newPhoneNumber, "電話番号は必須です");
        if (newPhoneNumber.length() > 30) {
            throw new IllegalArgumentException("電話番号が長すぎます（最大30文字）");
        }
        this.phoneNumber = newPhoneNumber;
    }

    public void setPrimaryEmail(UserEmail email) {
        this.primaryEmail = email;
        if (email != null && email.getUser() != this) {
            email.setUser(this);
        }
    }
    
    public void addEmail(UserEmail email) {
        if (email == null) return;
        // 同一メールの重複追加を避ける（equals/hashCode未実装でも動くようemailで判定）
        boolean exists = this.emails.stream()
            .anyMatch(e -> e.getEmail().equalsIgnoreCase(email.getEmail()));
        if (!exists) {
            this.emails.add(email);
        }
        if (email.getUser() != this) {
            email.setUser(this);
        }
    }

    // ===================================================
    // ================= ログイン状態の管理 =================
    // ===================================================

    /**
     * ログイン成功時に呼び出し、最終ログイン日時を現在時刻に更新し、ログイン失敗回数をリセットします。
     */
    public void markLoginSuccess() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginFailureCount = 0;
    }

    /**
     * ログイン失敗時に呼び出し、ログイン失敗回数を1増やします。
     */
    public void markLoginFailure() {
        this.loginFailureCount++;
    }

    // ==========================
    // ======= 論理削除処理 =======
    // ==========================

    /**
     * 論理削除を行います。 削除日時（deletedAt）を現在時刻に設定し、ユーザーのステータスを DELETED に変更します。 既に削除されている場合は何も行いません。
     * 関連エンティティ（LiveStream, Video, Subscription）も再帰的に論理削除します。
     */
    @Override
    public void softDelete() {
        super.softDelete();
        // 関連エンティティの再帰的論理削除
        softDeleteEntities(liveStreams);
        softDeleteEntities(videos);
        softDeleteEntities(following);
        softDeleteEntities(followers);
    }

    /**
     * 論理削除されたユーザーを復元します。 ステータスを ACTIVE に戻し、削除日時を null に設定します。 ステータスが DELETED でない場合は
     * IllegalStateException をスローします。 関連エンティティ（LiveStream, Video, Subscription）も再帰的に復元されます。
     *
     * @throws IllegalStateException 復元対象が削除状態でない場合
     */
    @Override
    public void restore() {
        super.restore();
        // 関連エンティティの再帰的復元
        restoreEntities(liveStreams);
        restoreEntities(videos);
        restoreEntities(following);
        restoreEntities(followers);
    }

    /**
     * エンティティの更新直前に呼び出されるコールバック。 ステータスと削除日時が一致していない場合（例：ステータスが ACTIVE なのに削除日時が存在する等）に例外をスローします。
     *
     * @throws IllegalStateException 状態と削除日時が矛盾している場合
     */
    @PreUpdate
    public void validateStateConsistency() {
        if ((status == UserStatus.DELETED) != (deletedAt != null)) {
            throw new IllegalStateException("削除ステータスと削除日時が一致していません");
        }
    }

    // ===================================================
    // ================= パスワード関連操作 =================
    // ===================================================

    /**
     * プレーンなパスワードをハッシュ化してセットします。
     *
     * @param rawPassword ハッシュ前のパスワード
     * @param encoder パスワードエンコーダー
     * @throws IllegalArgumentException パスワードまたはエンコーダーがnullまたは空の場合
     */
    public void hashAndSetPassword(String rawPassword, PasswordEncoder encoder) {
        Assert.hasText(rawPassword, "パスワード無いでっせ");
        Assert.notNull(encoder, "パスワードエンコーダー忘れてまっせ");
        this.password = encoder.encode(rawPassword);
    }

    /**
     * リメンバートークンを発行します。
     *
     * @param validDuration 有効期限の期間（null不可）
     * @throws IllegalArgumentException 有効期限がnullの場合
     */
    public void issueRememberToken(Duration validDuration) {
        Assert.notNull(validDuration, "有効期間が指定されていません");
        this.rememberToken = UUID.randomUUID().toString();
        this.rememberTokenExpiresAt = LocalDateTime.now().plus(validDuration);
    }

    /**
     * 渡されたトークンが有効かどうかを検証します。
     *
     * @param token トークン文字列（null不可）
     * @return true: 有効 / false: 無効
     */
    public boolean verifyRememberToken(String token) {
        return this.rememberToken != null && token != null && this.rememberToken.equals(token)
                && this.rememberTokenExpiresAt != null
                && this.rememberTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * パスワードを変更します。
     *
     * @param oldPassword 旧パスワード
     * @param newPassword 新パスワード
     * @param encoder パスワードエンコーダー
     */
    public void changePassword(String oldPassword, String newPassword, PasswordEncoder encoder) {
        Assert.hasText(oldPassword, "古いパスワード無いでっせ");
        Assert.hasText(newPassword, "新しいパスワード無いでっせ");
        Assert.notNull(encoder, "パスワードエンコーダー忘れてまっせ");

        if (!encoder.matches(oldPassword, this.password)) {
            throw new IllegalArgumentException("パスワード間違ってて草");
        }

        this.password = encoder.encode(newPassword);
    }

    // ===================================================
    // ================== プロフィール管理 ==================
    // ===================================================

    /**
     * ユーザーを名前とメールで初期化するコンストラクタ。
     *
     * @param name 表示名（null・空不可）
     * @param email メールアドレス（UserEmail の email, null・空不可）
     * @throws IllegalArgumentException 名前またはメールが不正な場合
     */
    public User(String name, String email) {
        Assert.hasText(name, "名前は必須です");
        Assert.hasText(email, "メールアドレスは必須です");
        this.name = name;
        UserEmail emailEntity = new UserEmail(email, this);
        this.setPrimaryEmail(emailEntity);  // 双方向リンクとisPrimaryをここで確実に同期
    }

    /**
     * ユーザーのロールを設定します。
     *
     * @param role ユーザー権限（null不可）
     * @throws IllegalArgumentException ロールがnullの場合
     */
    public void userSetRole(UserRole role) {
        Assert.notNull(role, "ロールが設定されていません");
        this.role = role;
    }

    /**
     * プロフィールを更新します。
     *
     * @param name 表示名（null・空不可）
     * @param profileImagePath プロフィール画像パス（null可）
     * @param coverImagePath カバー画像パス（null可）
     * @param bio 自己紹介（null可）
     * @throws IllegalArgumentException 名前が空の場合
     */
    public void updateProfile(String name, String profileImagePath, String coverImagePath,
            String bio) {
        Assert.hasText(name, "表示名は必須です");
        this.name = name;
        this.profileImagePath = profileImagePath;
        this.coverImagePath = coverImagePath;
        this.bio = bio;
    }

    /**
     * メールアドレスを変更します。変更された場合、認証フラグはリセットされます。
     *
     * @param newEmail 新しいメールアドレス（null・空不可）
     * @throws IllegalArgumentException メールが不正または既存メールと同一の場合
     */
    public void changeEmail(String newEmail) {
        Assert.hasText(newEmail, "メアド無いでっせ");
        final String normalized = newEmail.trim();

        // すでに同一なら何もしない（大小文字差異は無視する例）
        if (this.primaryEmail != null &&
            normalized.equalsIgnoreCase(this.primaryEmail.getEmail())) {
            return;
        }

        // 1) 旧primaryがあれば、先にemailsへ退避（orphan回避のため先にやる）
        if (this.primaryEmail != null) {
            this.addEmail(this.primaryEmail); // 双方向同期しつつ重複も吸収
        }

        // 2) 既存のサブメールに同じアドレスがあるなら「昇格」させる
        UserEmail existing =
            this.emails.stream()
                .filter(e -> normalized.equalsIgnoreCase(e.getEmail()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // primaryに昇格させ、リストからは外す（リストは“非primaryの集合”とする設計）
            this.setPrimaryEmail(existing); // 双方向同期
            this.emails.remove(existing);
        } else {
            // 3) なければ新規primaryを作成
            UserEmail fresh = new UserEmail(normalized, this); // いまのコンストラクタに合わせる
            this.setPrimaryEmail(fresh);                      // 双方向同期
            // CascadeType.PERSIST があれば user.save(...) で一緒にINSERTされる
        }

        // 4) 認証状態をリセット
        this.emailVerifiedAt = null;
    }

    // ===================================================
    // ================== プリファレンス管理 ==================
    // ===================================================

    /**
     * タイムゾーン・言語・生年月日を設定・更新します。
     *
     * @param timezone タイムゾーン（例: "Asia/Tokyo"）
     * @param language 表示言語コード（例: "ja"）
     * @param birthday 生年月日（null可）
     * @throws IllegalArgumentException 入力が不正な場合
     */
    public void updatePreferences(String timezone, String language, LocalDate birthday) {
        Assert.hasText(timezone, "タイムゾーンは必須です");
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("存在しないタイムゾーンです: " + timezone, e);
        }

        Assert.hasText(language, "言語は必須です");
        if (!List.of(Locale.getISOLanguages()).contains(language)) {
            throw new IllegalArgumentException("不正な言語コードです: " + language);
        }

        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生年月日が未来になっています: " + birthday);
        }

        this.timezone = timezone;
        this.language = language;
        this.birthday = birthday;
    }

    /**
     * タイムゾーンを個別に更新します。
     *
     * @param timezone タイムゾーン（例: "Asia/Tokyo"）
     * @throws IllegalArgumentException 不正なタイムゾーンの場合
     */
    public void updateTimezone(String timezone) {
        Assert.hasText(timezone, "タイムゾーンは必須です");
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("存在しないタイムゾーンです: " + timezone, e);
        }
        this.timezone = timezone;
    }

    /**
     * 表示言語を個別に更新します。
     *
     * @param language 言語コード（例: "ja", "en"）
     * @throws IllegalArgumentException 不正な言語コードの場合
     */
    public void updateLanguage(String language) {
        Assert.hasText(language, "言語は必須です");
        if (!List.of(Locale.getISOLanguages()).contains(language)) {
            throw new IllegalArgumentException("不正な言語コードです: " + language);
        }
        this.language = language;
    }

    /**
     * 生年月日を個別に設定します。
     *
     * @param birthday 生年月日（null可）
     * @throws IllegalArgumentException 生年月日が未来の場合
     */
    public void updateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生年月日が未来になっています: " + birthday);
        }
        this.birthday = birthday;
    }
}
