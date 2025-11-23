// app/signup/page.tsx
"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { LinkButton } from "@/components/ui/link-button";
import { signupUser } from "@/lib/auth/signup";
import { AuthCard } from "@/components/auth/auth-card";
import { AuthForm } from "@/components/auth/auth-form";

export default function SignupPage() {
  const router = useRouter();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (!name || !email || !password || !passwordConfirm) {
      setError("すべての項目を入力してください");
      return;
    }

    if (password !== passwordConfirm) {
      setError("パスワードが一致しません");
      return;
    }

    setLoading(true);

    const result = await signupUser({ name, email, password });

    if (!result.ok) {
      setError(result.message);
      setLoading(false);
      return;
    }

    router.push("/login?registered=1");
  };

  return (
    <AuthCard
      title="新規登録"
      footer={
        <div className="flex justify-between">
          <LinkButton href="/login" variant="link">
            すでにアカウントをお持ちの方はこちら
          </LinkButton>
          <LinkButton href="/forgot-password" variant="link">
            パスワードをお忘れですか？
          </LinkButton>
        </div>
      }
    >
      <AuthForm
        onSubmit={handleSubmit}
        errorMessage={error}
        submitLabel="登録する"
        loadingLabel="処理中…"
        loading={loading}
      >
        <Input
          type="text"
          placeholder="お名前"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <Input
          type="email"
          placeholder="メールアドレス"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <Input
          type="password"
          placeholder="パスワード"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <Input
          type="password"
          placeholder="パスワード（確認）"
          value={passwordConfirm}
          onChange={(e) => setPasswordConfirm(e.target.value)}
        />
      </AuthForm>
    </AuthCard>
  );
}
