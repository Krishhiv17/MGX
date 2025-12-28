"use client";

import { useEffect, useState } from "react";
import { topup } from "../lib/api/topup";
import { getPointsBalance } from "../lib/api/points";

export default function TopupForm() {
  const [mode, setMode] = useState<"points" | "mgc">("points");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [pointsAvailable, setPointsAvailable] = useState<string | null>(null);

  useEffect(() => {
    getPointsBalance()
      .then((data) => setPointsAvailable(data.pointsAvailable))
      .catch(() => setPointsAvailable(null));
  }, []);

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
      const response = await topup(mode === "points" ? numeric : undefined, mode === "mgc" ? numeric : undefined);
      setMessage(`Top-up complete: ${response.mgcCredited} MGC credited.`);
      setAmount("");
      const updated = await getPointsBalance();
      setPointsAvailable(updated.pointsAvailable);
    } catch (error) {
      setMessage("Top-up failed. Check balance and try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      <div className="flex gap-2">
        <button
          className={`rounded-full border px-4 py-1.5 text-xs font-semibold transition ${
            mode === "points"
              ? "border-black bg-black text-white"
              : "border-black/20 text-black hover:bg-black hover:text-white"
          }`}
          type="button"
          onClick={() => setMode("points")}
        >
          Points
        </button>
        <button
          className={`rounded-full border px-4 py-1.5 text-xs font-semibold transition ${
            mode === "mgc"
              ? "border-black bg-black text-white"
              : "border-black/20 text-black hover:bg-black hover:text-white"
          }`}
          type="button"
          onClick={() => setMode("mgc")}
        >
          MGC
        </button>
      </div>

      <label className="block text-sm">
        {mode === "points" ? "Points amount" : "MGC amount"}
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

      {mode === "points" && pointsAvailable ? (
        <p className="text-xs text-zinc-500">Available points: {pointsAvailable}</p>
      ) : null}

      {message ? <p className="text-sm text-zinc-600">{message}</p> : null}

      <button
        className="rounded-xl bg-black px-6 py-2 text-sm font-semibold text-white transition hover:bg-zinc-800 disabled:opacity-60"
        type="submit"
        disabled={loading}
      >
        {loading ? "Processing..." : "Submit top-up"}
      </button>
    </form>
  );
}
