// app/reset-password/page.tsx
"use client"

import { useState, FormEvent } from "react"
import { useSearchParams, useRouter } from "next/navigation"
import { useToast } from "@/components/ui/toast"
import { LinkButton } from "@/components/ui/link-button"
import { AuthCard } from "@/components/auth/auth-card"
import { AuthForm } from "@/components/auth/auth-form"
import { resetPassword } from "@/lib/auth/resetPassword"
import { InputWithLabel } from "@/components/ui/input-with-label"

export default function ResetPasswordPage() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const token = searchParams.get("token") // メールリンクのクエリから取得

  const [password, setPassword] = useState("")
  const [passwordConfirm, setPasswordConfirm] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { showToast } = useToast()

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError(null)

    if (!token) {
      setError(
        "パスワード再設定用のリンクが不正か、有効期限が切れています。もう一度メールのリンクからアクセスしてください。"
      )
      return
    }

    if (!password || !passwordConfirm) {
      setError("新しいパスワードと確認用パスワードを入力してください")
      return
    }

    if (password !== passwordConfirm) {
      setError("確認用パスワードが一致していません")
      return
    }

    // 必要ならここに長さ・文字種などのバリデーションを追加

    setLoading(true)

    try {
      const result = await resetPassword({ token, password })

      if (!result.ok) {
        // AuthResult がエラー詳細を持っているならそれを優先
        setError(result.message ?? "パスワードの再設定に失敗しました")
        return
      }

      showToast("パスワードを再設定しました。ログインしてください。")
      router.push("/login")
    } catch (err) {
      setError("エラーが発生しました。しばらくしてから再度お試しください。")
      console.log(err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthCard
      title="パスワード再設定"
      description="新しいパスワードを入力してください。"
      footer={
        <LinkButton href="/login" variant="link">
          ログイン画面に戻る
        </LinkButton>
      }
    >
      <AuthForm
        onSubmit={handleSubmit}
        errorMessage={error}
        submitLabel="パスワードを変更"
        loadingLabel="変更中..."
        loading={loading}
      >
        <div className="space-y-4">
            <InputWithLabel
              label="新しいパスワード"
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

            <InputWithLabel
              label="新しいパスワード（確認）"
              type="password"
              id="passwordConfirm"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
            />
        </div>
      </AuthForm>
    </AuthCard>
  )
}
