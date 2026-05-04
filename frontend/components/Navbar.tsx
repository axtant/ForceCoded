"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function Navbar() {
  const { username, logout } = useAuth();
  const router = useRouter();

  function handleLogout() {
    logout();
    router.push("/login");
  }

  return (
    <nav className="bg-gray-900 border-b border-gray-700 px-6 py-3 flex items-center justify-between">
      <div className="flex items-center gap-6">
        <Link href="/problems" className="text-green-400 font-bold text-lg tracking-tight">
          ForceCoded
        </Link>
        <Link href="/problems" className="text-gray-300 hover:text-white text-sm">
          Problems
        </Link>
        <Link href="/contests" className="text-gray-300 hover:text-white text-sm">
          Contests
        </Link>
      </div>

      <div className="flex items-center gap-4 text-sm">
        {username ? (
          <>
            <span className="text-gray-400">
              <span className="text-white font-medium">{username}</span>
            </span>
            <button
              onClick={handleLogout}
              className="text-gray-400 hover:text-white transition-colors"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link href="/login" className="text-gray-300 hover:text-white">
              Login
            </Link>
            <Link
              href="/register"
              className="bg-green-500 hover:bg-green-400 text-black font-medium px-3 py-1 rounded"
            >
              Register
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
