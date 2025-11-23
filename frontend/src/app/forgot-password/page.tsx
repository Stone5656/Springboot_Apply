// app/forgot-password/page.tsx
"use client"

import { useState, FormEvent } from "react"
import { useToast } from "@/components/ui/toast"
import { LinkButton } from "@/components/ui/link-button"
import { AuthCard } from "@/components/auth/auth-card"
import { AuthForm } from "@/components/auth/auth-form"
import { InputWithLabel } from "@/components/ui/input-with-label"

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { showToast } = useToast()

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError(null)

    if (!email) {
      setError("メールアドレスを入力してください")
      return
    }

    setLoading(true)

    try {
      // TODO: パスワードリセット用 API 呼び出し
      // await requestPasswordReset({ email })

      showToast(
        "パスワード再設定用のメールを送信しました。メールボックスをご確認ください。"
      )
    } catch (err) {
      setError("エラーが発生しました。しばらくしてから再度お試しください。")
      console.log(err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthCard
      title="パスワードをお忘れですか？"
      description="登録済みのメールアドレスを入力してください。パスワード再設定用のリンクをお送りします。"
      footer={
        <LinkButton href="/login" variant="link">
          ログイン画面に戻る
        </LinkButton>
      }
    >
      <AuthForm
        onSubmit={handleSubmit}
        errorMessage={error}
        submitLabel="メールを送信"
        loadingLabel="送信中..."
        loading={loading}
      >
        <InputWithLabel
          label="メールアドレス"
          type="email"
          id="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </AuthForm>
    </AuthCard>
  )
}
