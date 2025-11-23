// src/lib/auth/resetPassword.ts
import { apiClient } from "../apiClient";
import type { AuthResult } from "./types";
import { handleAuthError } from "./handleAuthError";

export type ResetPasswordInput = {
  token: string;
  password: string;
};

export async function resetPassword(
  input: ResetPasswordInput
): Promise<AuthResult> {
  try {
    // バックエンド: POST /api/users/reset-password
    await apiClient.post("/api/users/reset-password", input);

    return { ok: true };
  } catch (error) {
    return handleAuthError(error, "パスワードの再設定に失敗しました");
  }
}
