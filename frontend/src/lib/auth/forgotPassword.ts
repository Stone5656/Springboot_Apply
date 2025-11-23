// src/lib/auth/forgotPassword.ts
import { apiClient } from "../apiClient";
import type { AuthResult } from "./types";
import { handleAuthError } from "./handleAuthError";

export type ForgotPasswordInput = {
  email: string;
};

export async function requestPasswordReset(
  input: ForgotPasswordInput
): Promise<AuthResult> {
  try {
    // バックエンド側のエンドポイント名はプロジェクトに合わせて変更してください
    await apiClient.post("/api/users/forgot-password", input);

    // セキュリティ的には、メールアドレスの存在有無に関わらず
    // 同じレスポンスを返す設計にしておくのが一般的
    return { ok: true };
  } catch (error) {
    return handleAuthError(error, "パスワード再設定メールの送信に失敗しました");
  }
}
