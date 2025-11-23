// src/components/ui/input-with-label.tsx
"use client"

import { Input, InputProps } from "@/components/ui/input"

export type InputWithLabelProps = InputProps & {
  label: string
  htmlFor?: string
  error?: string | null
}

export function InputWithLabel({
  label,
  htmlFor,
  error,
  id,
  ...props
}: InputWithLabelProps) {
  // htmlFor優先、無ければid
  const inputId = htmlFor ?? id

  return (
    <div className="w-full space-y-1">
      <label
        htmlFor={inputId}
        className="block text-sm font-medium text-gray-700"
      >
        {label}
      </label>

      <Input id={inputId} {...props} />

      {error && (
        <p className="text-sm text-red-600">
          {error}
        </p>
      )}
    </div>
  )
}
