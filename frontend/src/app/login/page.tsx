// app/login/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Input } from "@/components/ui/input";
import { LinkButton } from "@/components/ui/link-button";
import { loginByEmailPassword } from "@/lib/auth/login";
import { useToast } from "@/components/ui/toast";
import { AuthCard } from "@/components/auth/auth-card";
import { AuthForm } from "@/components/auth/auth-form";

export default function LoginPage() {
  const router = useRouter();
  const params = useSearchParams();
  const registered = params.get("registered");
  const { showToast } = useToast();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (registered === "1") {
      showToast("登録が完了しました。ログインしてください。");
    }
  }, [registered, showToast]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (!email || !password) {
      setError("メールアドレスとパスワードを入力してください");
      return;
    }

    setLoading(true);

    const result = await loginByEmailPassword({ email, password });

    if (!result.ok) {
      setError(result.message);
      setLoading(false);
      return;
    }

    router.push("/dashboard");
  };

  return (
    <AuthCard
      title="ログイン"
      footer={
        <div className="flex justify-between">
          <LinkButton href="/signup" variant="link">
            新規登録はこちら
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
        submitLabel="ログイン"
        loadingLabel="処理中…"
        loading={loading}
      >
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
      </AuthForm>
    </AuthCard>
  );
}
