"use client";

import ProtectedRoute from "../../components/ProtectedRoute";
import TopupForm from "../../components/TopupForm";

export default function TopupPage() {
  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-transparent px-6 py-16">
        <div className="mx-auto w-full max-w-3xl rounded-3xl border border-black/10 bg-white p-8 shadow-sm dark:border-white/10 dark:bg-zinc-950">
          <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Top-up</p>
          <h1 className="mt-3 text-3xl font-semibold text-zinc-900 dark:text-white">
            Convert points to MGC
          </h1>
          <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
            Choose points or MGC to fund your wallet.
          </p>
          <div className="mt-8">
            <TopupForm />
          </div>
        </div>
      </main>
    </ProtectedRoute>
  );
}
