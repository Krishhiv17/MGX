"use client";

import { useEffect, useMemo, useState } from "react";
import { purchase, parseApiError, type PurchaseResponse } from "../lib/api/purchase";
import { getWallets, type Wallet } from "../lib/api/wallet";
import { getMgcUgcRate, type MgcUgcRate } from "../lib/api/rates";

export default function PurchaseForm({ gameId }: { gameId: string }) {
  const [mode, setMode] = useState<"mgc" | "ugc">("mgc");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [walletLoading, setWalletLoading] = useState(true);
  const [receipt, setReceipt] = useState<PurchaseResponse | null>(null);
  const [lastIdempotencyKey, setLastIdempotencyKey] = useState<string | null>(null);
  const [rate, setRate] = useState<MgcUgcRate | null>(null);
  const [rateLoading, setRateLoading] = useState(true);
  const [rateError, setRateError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setWalletLoading(true);
    getWallets()
      .then((data) => {
        if (active) {
          setWallets(data);
        }
      })
      .catch(() => {
        if (active) {
          setWallets([]);
        }
      })
      .finally(() => {
        if (active) {
          setWalletLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!gameId) {
      return;
    }
    let active = true;
    setRateLoading(true);
    setRateError(null);
    getMgcUgcRate(gameId)
      .then((data) => {
        if (active) {
          setRate(data);
        }
      })
      .catch((error) => {
        if (active) {
          setRate(null);
          setRateError(parseApiError(error));
        }
      })
      .finally(() => {
        if (active) {
          setRateLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [gameId]);

  const mgcBalance = useMemo(() => {
    const wallet = wallets.find((item) => item.type === "MGC" && !item.gameId);
    return wallet ? Number(wallet.balance) : 0;
  }, [wallets]);

  const ugcBalance = useMemo(() => {
    const wallet = wallets.find((item) => item.type === "UGC" && item.gameId === gameId);
    return wallet ? Number(wallet.balance) : 0;
  }, [wallets, gameId]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);
    setReceipt(null);

    try {
      const numeric = Number(amount);
      if (!numeric || Number.isNaN(numeric)) {
        setMessage("Enter a valid amount.");
        return;
      }
      const idempotencyKey = crypto.randomUUID();
      setLastIdempotencyKey(idempotencyKey);
      const response = await purchase(
        gameId,
        mode === "mgc" ? numeric : undefined,
        mode === "ugc" ? numeric : undefined,
        idempotencyKey
      );
      setMessage(`Purchase complete: ${response.ugcCredited} UGC credited.`);
      setReceipt(response);
      setAmount("");
      const refreshed = await getWallets();
      setWallets(refreshed);
    } catch (error) {
      setMessage(parseApiError(error));
    } finally {
      setLoading(false);
    }
  };

  const rateValue = rate ? Number(rate.ugcPerMgc) : null;
  const preview = useMemo(() => {
    const numeric = Number(amount);
    if (!rateValue || !numeric || Number.isNaN(numeric)) {
      return null;
    }
    if (mode === "mgc") {
      return {
        label: "Estimated UGC",
        value: (numeric * rateValue).toFixed(2),
      };
    }
    return {
      label: "Estimated MGC",
      value: (numeric / rateValue).toFixed(2),
    };
  }, [amount, mode, rateValue]);

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <div className="flex gap-2">
        <button
          className={`rounded-full border px-4 py-1.5 text-xs font-semibold transition ${
            mode === "mgc"
              ? "border-black bg-black text-white"
              : "border-black/20 text-black hover:bg-black hover:text-white"
          }`}
          type="button"
          onClick={() => setMode("mgc")}
        >
          Pay MGC
        </button>
        <button
          className={`rounded-full border px-4 py-1.5 text-xs font-semibold transition ${
            mode === "ugc"
              ? "border-black bg-black text-white"
              : "border-black/20 text-black hover:bg-black hover:text-white"
          }`}
          type="button"
          onClick={() => setMode("ugc")}
        >
          Receive UGC
        </button>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <div className="rounded-2xl border border-black/10 bg-zinc-50 p-4 text-sm text-zinc-600 dark:border-white/10 dark:bg-black dark:text-zinc-400">
          <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">MGC balance</p>
          <p className="mt-2 text-lg font-semibold text-zinc-900 dark:text-white">
            {walletLoading ? "--" : mgcBalance.toFixed(2)}
          </p>
        </div>
        <div className="rounded-2xl border border-black/10 bg-zinc-50 p-4 text-sm text-zinc-600 dark:border-white/10 dark:bg-black dark:text-zinc-400">
          <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">UGC balance</p>
          <p className="mt-2 text-lg font-semibold text-zinc-900 dark:text-white">
            {walletLoading ? "--" : ugcBalance.toFixed(2)}
          </p>
        </div>
      </div>

      <label className="block text-sm">
        {mode === "mgc" ? "MGC amount" : "UGC amount"}
        <input
          className="mt-1 w-full rounded-xl border border-black/10 px-4 py-2 text-sm outline-none focus:border-black dark:border-white/10 dark:bg-black dark:text-white"
          type="number"
          min="0"
          step="0.01"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          required
        />
      </label>

      <div className="rounded-2xl border border-black/10 bg-zinc-50 p-4 text-sm text-zinc-600 dark:border-white/10 dark:bg-black dark:text-zinc-400">
        <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Exchange rate</p>
        <p className="mt-2 text-base font-semibold text-zinc-900 dark:text-white">
          {rateLoading
            ? "Loading rate..."
            : rateValue
            ? `1 MGC = ${rateValue.toFixed(2)} UGC`
            : "Rate unavailable"}
        </p>
        {rateError ? <p className="mt-1 text-xs text-red-500">{rateError}</p> : null}
        {preview ? (
          <p className="mt-2 text-xs text-zinc-500">
            {preview.label}: {preview.value}
          </p>
        ) : null}
      </div>

      {message ? <p className="text-sm text-zinc-600">{message}</p> : null}
      {receipt ? (
        <div className="rounded-2xl border border-emerald-500/30 bg-emerald-50/60 p-4 text-sm text-emerald-900 dark:border-emerald-400/30 dark:bg-emerald-950/40 dark:text-emerald-200">
          <p className="text-xs uppercase tracking-[0.2em] text-emerald-700 dark:text-emerald-300">
            Purchase receipt
          </p>
          <div className="mt-2 grid gap-2 text-xs text-emerald-800 dark:text-emerald-200">
            <div>Transaction ID: {receipt.id}</div>
            <div>MGC spent: {receipt.mgcSpent}</div>
            <div>UGC credited: {receipt.ugcCredited}</div>
            <div>Rate snapshot: {receipt.rateUgcPerMgcSnapshot} UGC/MGC</div>
            <div>Idempotency key: {lastIdempotencyKey}</div>
          </div>
        </div>
      ) : null}

      <button
        className="rounded-xl bg-black px-6 py-2 text-sm font-semibold text-white transition hover:bg-zinc-800 disabled:opacity-60"
        type="submit"
        disabled={loading || !rateValue}
      >
        {loading ? "Processing..." : "Purchase"}
      </button>
    </form>
  );
}
