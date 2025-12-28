"use client";

import Link from "next/link";
import { useAuth } from "../contexts/AuthContext";

export default function NavBar() {
  const { user, logout, loading } = useAuth();

  return (
    <header className="w-full border-b border-black/10 bg-white/80 backdrop-blur dark:border-white/10 dark:bg-black/50">
      <div className="mx-auto flex w-full max-w-5xl items-center justify-between px-6 py-4">
        <Link className="text-lg font-semibold" href="/">
          MGX
        </Link>
        <nav className="flex items-center gap-4 text-sm">
          {loading ? null : user ? (
            <>
              <Link className="text-zinc-600 hover:text-black dark:text-zinc-300 dark:hover:text-white" href="/dashboard">
                Dashboard
              </Link>
              <span className="text-zinc-600 dark:text-zinc-300">{user.email}</span>
              <button
                className="rounded-full border border-black/20 px-4 py-1.5 text-sm transition hover:bg-black hover:text-white dark:border-white/20 dark:hover:bg-white dark:hover:text-black"
                onClick={logout}
                type="button"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link className="text-zinc-600 hover:text-black dark:text-zinc-300 dark:hover:text-white" href="/login">
                Login
              </Link>
              <Link
                className="rounded-full border border-black/20 px-4 py-1.5 text-sm transition hover:bg-black hover:text-white dark:border-white/20 dark:hover:bg-white dark:hover:text-black"
                href="/register"
              >
                Register
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
