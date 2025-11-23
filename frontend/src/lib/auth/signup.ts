// src/lib/auth/signup.ts
import { apiClient } from "../apiClient"
import type { AuthResult } from "./types"
import { handleAuthError } from "./handleAuthError"

export type SignupInput = {
  name: string
  email: string
  password: string
}

export async function signupUser(input: SignupInput): Promise<AuthResult> {
  try {
    // バックエンド側のエンドポイント名はプロジェクトに合わせて変更してください
    await apiClient.post("/api/users/register", input)
    return { ok: true }
  } catch (error) {
    return handleAuthError(error, "ユーザー登録に失敗しました")
  }
}
