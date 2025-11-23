// src/lib/auth/handleAuthError.ts
import { AxiosError } from "axios"
import { isApiErrorResponse } from "../apiError"
import type { AuthResult } from "./types"

export function handleAuthError(
  error: unknown,
  fallbackMessage: string
): AuthResult {
  if (error instanceof AxiosError) {
    const data = error.response?.data

    let message = fallbackMessage

    if (isApiErrorResponse(data) && typeof data.message === "string") {
      message = data.message
    }

    return { ok: false, message }
  }

  console.error(error)
  return { ok: false, message: "予期しないエラーが発生しました" }
}
