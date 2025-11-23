// src/lib/auth/login.ts
import { apiClient } from "../apiClient"
import type { AuthResult } from "./types"
import { handleAuthError } from "./handleAuthError"

export async function loginByEmailPassword(input: {
  email: string
  password: string
}): Promise<AuthResult> {
  try {
    await apiClient.post("/api/users/login", input)
    return { ok: true }
  } catch (error) {
    return handleAuthError(error, "ログインに失敗しました")
  }
}
