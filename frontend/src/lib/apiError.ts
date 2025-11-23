export type ApiErrorResponse = {
  message?: string
  code?: string
  errors?: Record<string, string[]>
}

export function isApiErrorResponse(x: unknown): x is ApiErrorResponse {
  if (typeof x !== "object" || x === null) return false

  const obj = x as Record<string, unknown>

  if ("message" in obj && typeof obj.message !== "string") return false

  if ("code" in obj && typeof obj.code !== "string") return false

  if ("errors" in obj && typeof obj.errors !== "object") return false

  return true
}
