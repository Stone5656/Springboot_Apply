"use client"

import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useCallback,
} from "react"
import { cn } from "@/lib/cn"
import { XMarkIcon } from "@heroicons/react/24/solid"

type ToastMessage = {
  id: number
  message: string
  visible: boolean
}

type ToastContextType = {
  showToast: (msg: string) => void
}

const ToastContext = createContext<ToastContextType | null>(null)

export const useToast = () => {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error("useToast must be used in ToastProvider")
  return ctx
}

export const ToastProvider = ({ children }: { children: ReactNode }) => {
  const [toasts, setToasts] = useState<ToastMessage[]>([])

  const showToast = useCallback((message: string) => {
    const id = Date.now()

    setToasts((prev) => [...prev, { id, message, visible: true }])

    // 自動 fade-out
    setTimeout(() => {
      setToasts((prev) =>
        prev.map((t) => (t.id === id ? { ...t, visible: false } : t))
      )
    }, 2700)

    // 自動削除
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 3000)
  }, [])

  const handleClose = useCallback((id: number) => {
    // まず非表示にして
    setToasts((prev) =>
      prev.map((t) => (t.id === id ? { ...t, visible: false } : t))
    )
    // アニメーション分少し待ってから削除
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 300)
  }, [])

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}

      {/* ↓ 画面下中央に配置 */}
      <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-[9999] flex flex-col items-center gap-3">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={cn(
              "relative min-w-[200px] max-w-[320px] rounded-md px-4 py-3 shadow-lg text-white bg-neutral-800 text-sm transition-all duration-300",
              t.visible
                ? "opacity-100 translate-y-0"
                : "opacity-0 translate-y-2"
            )}
          >
            {/* 閉じるボタン */}
            <button
              type="button"
              aria-label="閉じる"
              className="absolute top-1.5 right-1.5 inline-flex h-6 w-6 items-center justify-center rounded-full text-xs text-neutral-300 hover:bg-neutral-700 hover:text-white focus:outline-none focus:ring-2 focus:ring-neutral-500"
              onClick={() => handleClose(t.id)}
            >
              <XMarkIcon className="h-4 w-4" />
            </button>

            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
