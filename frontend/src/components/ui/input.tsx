// src/components/ui/input.tsx
"use client"

import * as React from "react"
import { cn } from "@/lib/cn"

export type InputProps = React.InputHTMLAttributes<HTMLInputElement>

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type = "text", ...props }, ref) => {
    return (
      <input
        type={type}
        ref={ref}
        className={cn(
          "w-full px-3 py-2 border border-gray-300 rounded-md",
          "text-sm leading-5",
          "focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-400",
          "disabled:cursor-not-allowed disabled:bg-gray-100",
          className,
        )}
        {...props}
      />
    )
  },
)

Input.displayName = "Input"
