"use client";

import { useEffect, useMemo, useState } from "react";
import ProtectedRoute from "../../components/ProtectedRoute";
import TransactionList, { type TransactionItem } from "../../components/TransactionList";
import { listPurchases } from "../../lib/api/purchase";
import { listTopups } from "../../lib/api/topup";

export default function TransactionsPage() {
  const [items, setItems] = useState<TransactionItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    Promise.all([listTopups(), listPurchases()])
      .then(([topups, purchases]) => {
        if (!active) {
          return;
        }
        const combined: TransactionItem[] = [
          ...topups.map((item) => ({ ...item, kind: "TOPUP" as const })),
          ...purchases.map((item) => ({ ...item, kind: "PURCHASE" as const })),
        ];
        setItems(combined);
      })
      .catch(() => {
        if (active) {
          setItems([]);
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

  const sorted = useMemo(() => {
    return [...items].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }, [items]);

  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-transparent px-6 py-16">
        <div className="mx-auto w-full max-w-4xl space-y-8">
          <div className="rounded-3xl border border-black/10 bg-white p-8 shadow-sm dark:border-white/10 dark:bg-zinc-950">
            <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Transactions</p>
            <h1 className="mt-3 text-3xl font-semibold text-zinc-900 dark:text-white">
              Activity log
            </h1>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              View recent top-ups and purchases.
            </p>
          </div>

          {loading ? (
            <p className="text-sm text-zinc-500">Loading transactions...</p>
          ) : (
            <TransactionList items={sorted} />
          )}
        </div>
      </main>
    </ProtectedRoute>
  );
}
