// src/lib/auth/types.ts
export type AuthResult =
  | { ok: true }
  | { ok: false; message: string }
