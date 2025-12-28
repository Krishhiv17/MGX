"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import ProtectedRoute from "../../components/ProtectedRoute";
import WalletCard from "../../components/WalletCard";
import { getWallets, type Wallet } from "../../lib/api/wallet";

export default function DashboardPage() {
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchWallets = useCallback(() => {
    let active = true;
    setLoading(true);
    setError(null);
    getWallets()
      .then((data) => {
        if (active) {
          setWallets(data);
        }
      })
      .catch(() => {
        if (active) {
          setWallets([]);
          setError("Unable to load wallets. Please refresh.");
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    const cleanup = fetchWallets();
    return () => cleanup();
  }, [fetchWallets]);

  const ugcWallets = useMemo(
    () => wallets.filter((wallet) => wallet.type === "UGC"),
    [wallets]
  );
  const getBalance = useCallback(
    (type: Wallet["type"]) => {
      const wallet = wallets.find((item) => item.type === type && !item.gameId);
      return wallet ? Number(wallet.balance) : 0;
    },
    [wallets]
  );

  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-transparent px-6 py-16">
        <div className="mx-auto w-full max-w-5xl space-y-10">
          <section className="rounded-3xl border border-black/10 bg-white p-8 shadow-sm dark:border-white/10 dark:bg-zinc-950">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Wallet overview</p>
                <h1 className="mt-3 text-3xl font-semibold text-zinc-900 dark:text-white">
                  Your balances
                </h1>
              </div>
              <div className="flex flex-wrap gap-3">
                <Link
                  className="rounded-full bg-black px-5 py-2 text-sm font-semibold text-white transition hover:bg-zinc-800"
                  href="/topup"
                >
                  Top-up
                </Link>
                <Link
                  className="rounded-full border border-black/20 px-5 py-2 text-sm font-semibold text-black transition hover:bg-black hover:text-white dark:border-white/20 dark:text-white dark:hover:bg-white dark:hover:text-black"
                  href="/games"
                >
                  Browse games
                </Link>
                <Link
                  className="rounded-full border border-black/20 px-5 py-2 text-sm font-semibold text-black transition hover:bg-black hover:text-white dark:border-white/20 dark:text-white dark:hover:bg-white dark:hover:text-black"
                  href="/transactions"
                >
                  Transactions
                </Link>
                <button
                  className="rounded-full border border-black/20 px-5 py-2 text-sm font-semibold text-black transition hover:bg-black hover:text-white dark:border-white/20 dark:text-white dark:hover:bg-white dark:hover:text-black"
                  type="button"
                  onClick={fetchWallets}
                >
                  Refresh
                </button>
              </div>
            </div>

            <div className="mt-8 grid gap-4 md:grid-cols-2">
              <div className="rounded-2xl border border-black/10 bg-zinc-50 p-5 dark:border-white/10 dark:bg-black">
                <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Reward points</p>
                <p className="mt-3 text-2xl font-semibold text-zinc-900 dark:text-white">
                  {getBalance("REWARD_POINTS").toFixed(2)}
                </p>
              </div>
              <div className="rounded-2xl border border-black/10 bg-zinc-50 p-5 dark:border-white/10 dark:bg-black">
                <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">MGC</p>
                <p className="mt-3 text-2xl font-semibold text-zinc-900 dark:text-white">
                  {getBalance("MGC").toFixed(2)}
                </p>
              </div>
            </div>

            <div className="mt-6">
              {loading ? (
                <div className="text-sm text-zinc-500">Loading wallets...</div>
              ) : error ? (
                <div className="text-sm text-red-500">{error}</div>
              ) : null}
            </div>
          </section>

          <section className="rounded-3xl border border-black/10 bg-white p-8 shadow-sm dark:border-white/10 dark:bg-zinc-950">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">UGC wallets</p>
                <h2 className="mt-3 text-2xl font-semibold text-zinc-900 dark:text-white">
                  Game-specific balances
                </h2>
              </div>
              <Link className="text-sm text-zinc-600 hover:text-black dark:text-zinc-400" href="/games">
                Purchase more
              </Link>
            </div>

            <div className="mt-8 grid gap-4 md:grid-cols-2">
              {loading ? (
                <div className="col-span-full text-sm text-zinc-500">Loading UGC wallets...</div>
              ) : ugcWallets.length ? (
                ugcWallets.map((wallet) => <WalletCard key={wallet.id} wallet={wallet} />)
              ) : (
                <div className="col-span-full rounded-2xl border border-dashed border-black/20 bg-white/50 p-6 text-sm text-zinc-500 dark:border-white/20 dark:bg-black/40">
                  No UGC wallets yet. Purchase a game to mint UGC.
                </div>
              )}
            </div>
          </section>
        </div>
      </main>
    </ProtectedRoute>
  );
}
