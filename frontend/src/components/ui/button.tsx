// src/components/ui/button.tsx
"use client"

import * as React from "react"
import { cn } from "@/lib/cn"

type Variant = "primary" | "outline" | "ghost"

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  fullWidth?: boolean
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", fullWidth, ...props }, ref) => {
    const base =
      "inline-flex items-center justify-center px-3 py-2 rounded-md text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"

    const variants: Record<Variant, string> = {
      primary: "bg-gray-800 text-white hover:bg-gray-900",
      outline:
        "border border-gray-300 text-gray-800 hover:bg-gray-50 bg-white",
      ghost: "text-gray-800 hover:bg-gray-100",
    }

    return (
      <button
        ref={ref}
        className={cn(
          base,
          variants[variant],
          fullWidth && "w-full",
          className,
        )}
        {...props}
      />
    )
  },
)

Button.displayName = "Button"
