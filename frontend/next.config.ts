import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${process.env.BACKEND_URL}/api/:path*`,
      },
      {
        source: "/ws/:path*",
        destination: `${process.env.BACKEND_URL}/ws/:path*`,
      },
    ];
  },
};

export default nextConfig;
