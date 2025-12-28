"use client";

import { useState } from "react";
import { purchase } from "../lib/api/purchase";

export default function PurchaseForm({ gameId }: { gameId: string }) {
  const [mode, setMode] = useState<"mgc" | "ugc">("mgc");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      const numeric = Number(amount);
      if (!numeric || Number.isNaN(numeric)) {
        setMessage("Enter a valid amount.");
        return;
      }
      const response = await purchase(
        gameId,
        mode === "mgc" ? numeric : undefined,
        mode === "ugc" ? numeric : undefined
      );
      setMessage(`Purchase complete: ${response.ugcCredited} UGC credited.`);
      setAmount("");
    } catch (error) {
      setMessage("Purchase failed. Check balance and try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
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

      {message ? <p className="text-sm text-zinc-600">{message}</p> : null}

      <button
        className="rounded-xl bg-black px-6 py-2 text-sm font-semibold text-white transition hover:bg-zinc-800 disabled:opacity-60"
        type="submit"
        disabled={loading}
      >
        {loading ? "Processing..." : "Purchase"}
      </button>
    </form>
  );
}
