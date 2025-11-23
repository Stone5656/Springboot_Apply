// next.config.ts
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  allowedDevOrigins: [
    "http://localhost:4000",
    "http://127.0.0.1:4000",
  ],
};

export default nextConfig;
