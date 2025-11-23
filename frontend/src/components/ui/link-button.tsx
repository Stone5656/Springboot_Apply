// src/components/ui/link-button.tsx
"use client"

import * as React from "react"
import Link, { type LinkProps } from "next/link"
import { cn } from "@/lib/cn"

type Variant = "link" | "button"

type AnchorProps = Omit<
  React.AnchorHTMLAttributes<HTMLAnchorElement>,
  "href"
>

export interface LinkButtonProps
  extends LinkProps, AnchorProps {
  variant?: Variant
}

export const LinkButton: React.FC<LinkButtonProps> = ({
  href,
  className,
  children,
  variant = "link",
  ...props
}) => {
  const base = "inline-flex items-center text-sm"

  const variants: Record<Variant, string> = {
    link: "text-blue-600 hover:underline",
    button:
      "px-3 py-2 rounded-md bg-gray-800 text-white hover:bg-gray-900 text-sm",
  }

  return (
    <Link
      href={href}
      className={cn(base, variants[variant], className)}
      {...props}
    >
      {children}
    </Link>
  )
}
