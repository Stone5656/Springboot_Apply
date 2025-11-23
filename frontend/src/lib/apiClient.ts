// src/lib/apiClient.ts
import axios from "axios"

export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:9000",
  headers: {
    "Content-Type": "application/json",
  },
})
