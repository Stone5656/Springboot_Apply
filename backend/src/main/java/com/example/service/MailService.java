// src/main/java/com/example/service/MailService.java
package com.example.service;

import com.example.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * メール送信を担当するサービスクラス。
 *
 * 現時点では「パスワードリセット用メール」のみを扱う。
 */
@Service
@RequiredArgsConstructor
public class MailService {

    public MailService(){
        this.mailSender = null;
    }

    private final JavaMailSender mailSender;

    /**
     * フロントエンドのベースURL。
     * 例: https://example.com
     *
     * application.yml などで app.frontend-base-url を設定しておく前提。
     */
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    /**
     * 送信元メールアドレス。
     * spring.mail.username と同じでもよいが、環境に合わせて調整すること。
     */
    @Value("${app.mail.from}")
    private String fromAddress;

    /**
     * パスワードリセット用メールを送信する。
     *
     * @param user     対象ユーザー
     * @param rawToken メールに埋め込む生トークン
     */
    public void sendPasswordResetMail(User user, String rawToken) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("rawToken must not be blank");
        }
        if (frontendBaseUrl == null || frontendBaseUrl.isBlank()) {
            // 設定漏れの場合は実行時に気付けるようにしておく
            throw new IllegalStateException("app.frontend-base-url が設定されていません");
        }

        // 主メールアドレスの取得（null のまま運用しているとここで落ちる）
        if (user.getPrimaryEmail() == null || user.getPrimaryEmail().getEmail() == null) {
            throw new IllegalStateException("ユーザーの主メールアドレスが設定されていません");
        }
        String toAddress = user.getPrimaryEmail().getEmail();

        // フロントエンド側の reset-password 画面に生トークンを付与して飛ばす
        String resetUrl = String.format(
            "%s/reset-password?token=%s",
            frontendBaseUrl,
            rawToken
        );

        String subject = "【○○サービス】パスワード再設定のご案内";
        String text = """
                %s 様

                パスワード再設定のご依頼を受け付けました。
                下記のリンクからパスワードの再設定を行ってください。

                %s

                ※このリンクの有効期限は 30 分です。
                ※このメールに心当たりがない場合は、このメールは破棄してください。
                """
            .formatted(user.getName(), resetUrl);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
