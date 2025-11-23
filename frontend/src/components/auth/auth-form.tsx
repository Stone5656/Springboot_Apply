// src/components/auth/auth-form.tsx
"use client"

import type { ReactNode, FormHTMLAttributes } from "react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/cn"

type AuthFormProps = {
  /** フォーム本体の中身（Input やラベルなど） */
  children: ReactNode
  /** バリデーションやAPIエラーなど */
  errorMessage?: string | null
  /** 送信ボタンの通常時ラベル */
  submitLabel: string
  /** ローディング中のラベル（未指定なら submitLabel を使う） */
  loadingLabel?: string
  /** ローディング状態 */
  loading?: boolean
  /** フォーム全体に追加したいクラス */
  className?: string
} & Omit<FormHTMLAttributes<HTMLFormElement>, "children">

export function AuthForm({
  children,
  errorMessage,
  submitLabel,
  loadingLabel,
  loading = false,
  className,
  ...formProps
}: AuthFormProps) {
  return (
    <form
      {...formProps}
      className={cn("space-y-4", className)}
    >
      {/* 上部のフィールド群 */}
      <div className="flex flex-col gap-3">
        {children}
      </div>

      {/* エラー表示（あれば） */}
      {errorMessage && (
        <p className="text-sm text-red-600">
          {errorMessage}
        </p>
      )}

      {/* 送信ボタン */}
      <Button
        type="submit"
        fullWidth
        disabled={loading}
      >
        {loading ? (loadingLabel ?? submitLabel) : submitLabel}
      </Button>
    </form>
  )
}
