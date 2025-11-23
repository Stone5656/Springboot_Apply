// src/components/auth/auth-card.tsx
"use client"

import { ReactNode } from "react"
import { cn } from "@/lib/cn"

type AuthCardProps = {
  title: string
  description?: string
  children: ReactNode
  footer?: ReactNode
  className?: string
}

export function AuthCard({
  title,
  description,
  children,
  footer,
  className,
}: AuthCardProps) {
  return (
    <div
      className={cn(
        "flex min-h-svh items-center justify-center bg-gray-100",
        className
      )}
    >
      <div
        className={cn(
          "w-full max-w-md rounded-lg bg-white p-8 shadow-md"
        )}
      >
        <h1 className="text-xl font-semibold text-gray-900">{title}</h1>

        {description && (
          <p className="mt-2 text-sm text-gray-600">{description}</p>
        )}

        <div className="mt-6">{children}</div>

        {footer && <div className="mt-4 text-center">{footer}</div>}
      </div>
    </div>
  )
}
